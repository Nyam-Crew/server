package com.nyam.everyday.module.ranking.service;

import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.ranking.entity.MemberGlobalRanking;
import com.nyam.everyday.module.ranking.entity.MemberTeamRanking;
import com.nyam.everyday.module.ranking.entity.TeamGlobalRanking;
import com.nyam.everyday.module.ranking.repository.MemberGlobalRankingRepository;
import com.nyam.everyday.module.ranking.repository.MemberTeamRankingRepository;
import com.nyam.everyday.module.ranking.repository.TeamGlobalRankingRepository;
import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.module.team.repository.TeamRepository;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 정기적으로 Redis의 실시간 랭킹 데이터를 RDBMS에 영구 보관(아카이빙)하는 서비스입니다.
 */
@Slf4j
@Service
public class RankingArchivingService {

    private static final int BATCH_SIZE = 1000; // 한 번에 스캔하고 처리할 랭킹 데이터 수

    @Qualifier("redisRankingTemplate")
    private final RedisTemplate<String, String> redisTemplate;
    private final RankingKeys keys;
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final MemberGlobalRankingRepository memberGlobalRankingRepository;
    private final TeamGlobalRankingRepository teamGlobalRankingRepository;
    private final MemberTeamRankingRepository memberTeamRankingRepository;

    private final Clock clock = Clock.systemDefaultZone();

    public RankingArchivingService(@Qualifier("redisRankingTemplate") RedisTemplate<String, String> redisTemplate,
        RankingKeys keys,
        MemberRepository memberRepository,
        TeamRepository teamRepository,
        MemberGlobalRankingRepository memberGlobalRankingRepository,
        TeamGlobalRankingRepository teamGlobalRankingRepository,
        MemberTeamRankingRepository memberTeamRankingRepository
        ) {
        this.redisTemplate = redisTemplate;
        this.keys = keys;
        this.memberRepository = memberRepository;
        this.teamRepository = teamRepository;
        this.memberGlobalRankingRepository = memberGlobalRankingRepository;
        this.teamGlobalRankingRepository = teamGlobalRankingRepository;
        this.memberTeamRankingRepository = memberTeamRankingRepository;
    }

    // ================= 월초: 팀 멤버수 스냅샷 =================

    /**
     * [매월 1일 00:00] 팀 간 랭킹의 공정성을 위해 모든 팀의 현재 멤버 수를 스냅샷으로 Redis에 저장합니다.
     */
    @Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    public void snapshotMonthlyTeamMemberCounts() {
        var now = ZonedDateTime.now(clock).withZoneSameInstant(keys.zoneId());
        var monthly = keys.monthlySuffix(now);
        var h = redisTemplate.opsForHash();
        var hashKey = keys.teamMemberCountMonthlyHash(monthly);
        var teams = teamRepository.findAll();
        Map<Object, Object> map = new HashMap<>(teams.size());
        for (Team t : teams) {
            int count = Math.max(1, t.getTeamCurrentMembers());
            map.put(Long.toString(t.getTeamId()), Integer.toString(count));
        }
        if (!map.isEmpty()) {
            h.putAll(hashKey, map);
            log.info("[snapshot] saved team member counts for {} ({} teams)", monthly, map.size());
        } else {
            log.info("[snapshot] no teams to snapshot for {}", monthly);
        }
    }

    // ================= 월간 아카이브(지난달) =================

    /**
     * [매월 1일 00:05] 지난달의 개인 랭킹과 팀 간 랭킹을 Redis에서 RDBMS로 아카이빙합니다.
     */
    @Scheduled(cron = "0 5 0 1 * *", zone = "Asia/Seoul")
    @Transactional
    public void archiveMonthlyRankings() {
        var now = ZonedDateTime.now(clock).withZoneSameInstant(keys.zoneId());
        var lastMonth = YearMonth.from(now).minusMonths(1);
        String ms = keys.monthlySuffix(lastMonth);
        int u = archiveMonthlyUserRanking(ms, lastMonth);
        int t = archiveMonthlyInterTeamRanking(ms, lastMonth);
        log.info("[archive-monthly] user={}, interTeam={}, month={}", u, t, ms);
    }

    /**
     * 지난달의 개인 랭킹을 아카이빙합니다. (템플릿 메소드 사용)
     */
    private int archiveMonthlyUserRanking(String monthlySuffix, YearMonth lastMonth) {
        var rank = new AtomicLong(1);
        return archiveRankingTemplate(
            keys.userMonthlyKey(monthlySuffix),
            batch -> {
                var memberIds = batch.stream().map(t -> Long.parseLong(t.getValue())).toList();
                var memberMap = memberRepository.findAllById(memberIds).stream()
                    .collect(Collectors.toMap(Member::getMemberId, Function.identity()));
                var rows = new ArrayList<MemberGlobalRanking>();
                for (var t : batch) {
                    var member = memberMap.get(Long.parseLong(t.getValue()));
                    if (member == null) continue;
                    long total = toLongScore(t.getScore());
                    rows.add(new MemberGlobalRanking(null, lastMonth.getYear(), lastMonth.getMonthValue(),
                        member, (int) clipInt(total), (int) rank.getAndIncrement()));
                }
                return rows;
            },
            memberGlobalRankingRepository::saveAll,
            "user-monthly"
        );
    }

    /**
     * 지난달의 팀 간 랭킹을 아카이빙합니다. (고유 로직으로 인해 별도 구현)
     */
    private int archiveMonthlyInterTeamRanking(String monthlySuffix, YearMonth lastMonth) {
        var avgSrc = keys.interTeamMonthlyKey(monthlySuffix);
        var sumSrc = keys.teamScoreSumMonthlyKey(monthlySuffix);
        var avgSnap = keys.snapshotKey(avgSrc);
        var sumSnap = keys.snapshotKey(sumSrc);
        boolean hasAvg = renameIfExists(avgSrc, avgSnap);
        boolean hasSum = renameIfExists(sumSrc, sumSnap);
        if (!hasAvg) {
            log.info("[archive team] no avg key: {}", avgSrc);
            if (hasSum) renameIfExists(sumSnap, sumSrc);
            return 0;
        }
        try {
            var rows = new ArrayList<TeamGlobalRanking>();
            var rank = new AtomicLong(1);
            Map<String, Double> totalScoreMap = new HashMap<>();
            if (hasSum) {
                var sumTuples = redisTemplate.opsForZSet().rangeWithScores(sumSnap, 0, -1);
                if (sumTuples != null) {
                    sumTuples.forEach(t -> totalScoreMap.put(t.getValue(), t.getScore()));
                }
            }
            processZSetInScoreOrder(avgSnap, BATCH_SIZE, batch -> {
                var teamIds = batch.stream().map(t -> Long.parseLong(t.getValue())).toList();
                var teamMap = teamRepository.findAllById(teamIds).stream()
                    .collect(Collectors.toMap(Team::getTeamId, Function.identity()));
                for (var t : batch) {
                    Long teamId = Long.parseLong(t.getValue());
                    Team team = teamMap.get(teamId);
                    if (team == null) continue;
                    double avg = (t.getScore() != null) ? t.getScore() : 0.0;
                    long total = toLongScore(totalScoreMap.get(String.valueOf(teamId)));
                    int memberCount = Math.max(1, team.getTeamCurrentMembers());
                    rows.add(new TeamGlobalRanking(null, lastMonth.getYear(), lastMonth.getMonthValue(),
                        team, (int) rank.getAndIncrement(), BigDecimal.valueOf(avg), total, memberCount));
                }
            });
            if (!rows.isEmpty()) {
                teamGlobalRankingRepository.saveAll(rows);
            }
            redisTemplate.delete(avgSnap);
            if (hasSum) redisTemplate.delete(sumSnap);
            return rows.size();
        } catch (Exception e) {
            log.error("[archive team] FAILED to archive {}. Restoring snapshot keys.", avgSnap, e);
            renameIfExists(avgSnap, avgSrc);
            if (hasSum) renameIfExists(sumSnap, sumSrc);
            throw new RuntimeException("Failed to archive team ranking", e);
        }
    }

    // ================= 주간 아카이브(지난주, 활성팀 Set 기반) =================

    /**
     * [매주 월요일 00:00] 지난주에 활동이 있었던 모든 팀의 팀 내 랭킹을 아카이빙합니다.
     */
    @Scheduled(cron = "0 0 0 ? * MON", zone = "Asia/Seoul")
    public void archiveWeeklyIntraTeamRankings() {
        var now = ZonedDateTime.now(clock).withZoneSameInstant(keys.zoneId());
        var lastWeek = now.toLocalDate().minusWeeks(1);
        String ws = keys.weeklySuffix(lastWeek);
        var activeKey = keys.activeIntraTeamWeeklyKey(ws);
        var activeTeamIdsStr = redisTemplate.opsForSet().members(activeKey);
        if (activeTeamIdsStr == null || activeTeamIdsStr.isEmpty()) {
            log.info("[archive-weekly] no active teams set for {}", ws);
            return;
        }
        var activeTeamIds = activeTeamIdsStr.stream().map(Long::parseLong).toList();
        var activeTeams = teamRepository.findAllById(activeTeamIds);
        int totalSaved = 0;
        for (Team team : activeTeams) {
            try {
                totalSaved += archiveIntraTeamRankingForTeam(team, ws);
            } catch (Exception e) {
                log.error("[archive-weekly] FAILED for teamId {}. Skipping.", team.getTeamId(), e);
            }
        }
        redisTemplate.delete(activeKey);
        log.info("[archive-weekly] archived {}, cleared active set: {}", totalSaved, activeKey);
    }

    /**
     * 특정 팀의 지난주 팀 내 랭킹을 아카이빙합니다. (템플릿 메소드 사용)
     */
    @Transactional
    public int archiveIntraTeamRankingForTeam(Team team, String weeklySuffix) {
        var wf = WeekFields.of(Locale.KOREA);
        var sampleDate = parseWeekSuffixToLocalDate(weeklySuffix);
        int year = sampleDate.get(wf.weekBasedYear());
        int week = sampleDate.get(wf.weekOfWeekBasedYear());
        var rank = new AtomicLong(1);

        return archiveRankingTemplate(
            keys.intraTeamWeeklyKey(team.getTeamId(), weeklySuffix),
            batch -> {
                var memberIds = batch.stream().map(t -> Long.parseLong(t.getValue())).toList();
                var memberMap = memberRepository.findAllById(memberIds).stream()
                    .collect(Collectors.toMap(Member::getMemberId, Function.identity()));
                var rows = new ArrayList<MemberTeamRanking>();
                for (var t : batch) {
                    var m = memberMap.get(Long.parseLong(t.getValue()));
                    if (m == null) continue;
                    long total = toLongScore(t.getScore());
                    rows.add(new MemberTeamRanking(null, year, week, team, m, (int) rank.getAndIncrement(), (int) clipInt(total)));
                }
                return rows;
            },
            memberTeamRankingRepository::saveAll,
            "intra-team"
        );
    }

    // ================= 아카이빙 템플릿 =================

    /**
     * 랭킹 아카이빙의 공통 로직(스냅샷, 배치 처리, 저장, 롤백)을 처리하는 제네릭 템플릿 메소드입니다.
     * @param sourceKey 원본 Redis 키
     * @param dataProcessor Redis 데이터를 DB 엔티티 리스트로 변환하는 함수
     * @param batchSaver 변환된 엔티티 리스트를 DB에 저장하는 함수
     * @param logContext 로그 식별을 위한 문자열
     * @return 아카이빙된 행의 수
     * @param <E> 아카이빙될 엔티티의 타입
     */
    private <E> int archiveRankingTemplate(
        String sourceKey,
        Function<List<TypedTuple<String>>, List<E>> dataProcessor,
        Consumer<List<E>> batchSaver,
        String logContext
    ) {
        String snapshotKey = keys.snapshotKey(sourceKey);
        if (!renameIfExists(sourceKey, snapshotKey)) {
            log.info("[archive {}] no key: {}", logContext, sourceKey);
            return 0;
        }

        try {
            List<E> allRows = new ArrayList<>();
            processZSetInScoreOrder(snapshotKey, BATCH_SIZE, batch -> {
                List<E> processedRows = dataProcessor.apply(batch);
                allRows.addAll(processedRows);
            });

            if (!allRows.isEmpty()) {
                batchSaver.accept(allRows);
            }
            redisTemplate.delete(snapshotKey);
            return allRows.size();
        } catch (Exception e) {
            log.error("[archive {}] FAILED to archive {}. Restoring snapshot key.", logContext, snapshotKey, e);
            renameIfExists(snapshotKey, sourceKey); // 롤백
            throw new RuntimeException("Failed to archive " + logContext, e);
        }
    }

    // ================= Redis/도우미 =================

    /** ZSet을 스코어 역순으로 페이지네이션하여 처리합니다. */
    private void processZSetInScoreOrder(String key, int pageSize, Consumer<List<TypedTuple<String>>> pageConsumer) {
        Long size = redisTemplate.opsForZSet().zCard(key);
        if (size == null || size == 0) return;
        long total = size;
        for (long start = 0; start < total; start += pageSize) {
            long end = Math.min(total - 1, start + pageSize - 1);
            Set<TypedTuple<String>> page = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
            if (page == null || page.isEmpty()) break;
            pageConsumer.accept(new ArrayList<>(page));
        }
    }

    /** Redis 키의 이름을 원자적으로 변경하고, 키가 존재했을 경우에만 true를 반환합니다. */
    private boolean renameIfExists(String from, String to) {
        return Boolean.TRUE.equals(redisTemplate.execute((RedisConnection conn) -> {
            var f = keys.bytes(from);
            var t = keys.bytes(to);
            boolean exists = conn.keyCommands().exists(f);
            if (!exists) return false;
            conn.keyCommands().rename(f, t);
            return true;
        }));
    }

    /** Double 스코어를 long으로 변환합니다. */
    private static long toLongScore(Double s) { return (s != null) ? Math.round(s) : 0L; }

    /** long 값을 Integer 범위로 안전하게 변환합니다. */
    private static long clipInt(long value) {
        return Math.min(Integer.MAX_VALUE, Math.max(Integer.MIN_VALUE, value));
    }

    /** "yyyy-ww" 형식의 주차 정보를 해당 주의 월요일 날짜로 변환합니다. */
    private static LocalDate parseWeekSuffixToLocalDate(String weeklySuffix) {
        String[] parts = weeklySuffix.split("-");
        int year = Integer.parseInt(parts[0]);
        int week = Integer.parseInt(parts[1]);
        var wf = WeekFields.of(Locale.KOREA);
        return LocalDate.now()
            .with(wf.weekBasedYear(), year)
            .with(wf.weekOfWeekBasedYear(), week)
            .with(wf.dayOfWeek(), 1);
    }
}