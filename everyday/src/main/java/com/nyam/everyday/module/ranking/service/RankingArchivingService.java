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
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Redis의 실시간 랭킹 데이터를 주기적으로 RDBMS에 영구 보관(아카이빙)하는 서비스.
 * - 월초 팀 멤버수 스냅샷
 * - 지난달 개인/팀간 월간 랭킹 아카이브
 * - 지난주 팀내 주간 랭킹 아카이브 (활성팀 SET 기반)
 *
 * 주요 강화점:
 * 1) ISO 주차 기준 일원화
 * 2) Redis Cluster 환경에서 RENAME 실패 시 COPY/DUMP-RESTORE 폴백
 * 3) 아카이브 시 페이지 단위 즉시 저장(메모리 안전)
 * 4) 팀 월간 아카이브의 멤버수는 월초 스냅샷(Hash) 우선 사용
 * 5) 스케줄 CRON은 프로퍼티로 오버라이드 가능
 */
@Slf4j
@Service
public class RankingArchivingService {

    private static final int BATCH_SIZE = 1000; // 페이지당 처리 개수
    private static final WeekFields WF_ISO = WeekFields.ISO;

    private final RedisTemplate<String, String> redisTemplate;
    private final RankingKeys keys;
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final MemberGlobalRankingRepository memberGlobalRankingRepository;
    private final TeamGlobalRankingRepository teamGlobalRankingRepository;
    private final MemberTeamRankingRepository memberTeamRankingRepository;
    private final Clock clock;

    // 스케줄 오버라이드용 (기본 값은 기존과 동일)
    @Value("${ranking.cron.zone:Asia/Seoul}")
    private String cronZone;

    @Value("${ranking.cron.snapshot.monthly:0 0 0 1 * *}")
    private String cronMonthlySnapshot;

    @Value("${ranking.cron.archive.monthly:0 5 0 1 * *}")
    private String cronMonthlyArchive;

    @Value("${ranking.cron.archive.weekly:0 0 0 ? * MON}")
    private String cronWeeklyArchive;

    public RankingArchivingService(
        @Qualifier("redisRankingTemplate") RedisTemplate<String, String> redisTemplate,
        RankingKeys keys,
        MemberRepository memberRepository,
        TeamRepository teamRepository,
        MemberGlobalRankingRepository memberGlobalRankingRepository,
        TeamGlobalRankingRepository teamGlobalRankingRepository,
        MemberTeamRankingRepository memberTeamRankingRepository,
        Clock clock
    ) {
        this.redisTemplate = redisTemplate;
        this.keys = keys;
        this.memberRepository = memberRepository;
        this.teamRepository = teamRepository;
        this.memberGlobalRankingRepository = memberGlobalRankingRepository;
        this.teamGlobalRankingRepository = teamGlobalRankingRepository;
        this.memberTeamRankingRepository = memberTeamRankingRepository;
        this.clock = clock;
    }

    // =========================================================
    // 월초: 팀 멤버수 스냅샷
    // =========================================================

    /**
     * [매월 1일 00:00 (기본)] 팀 평균 공정성 유지를 위해 월초의 팀 멤버 수를 Hash로 스냅샷 저장.
     */
    @Scheduled(cron = "${ranking.cron.snapshot.monthly:0 0 0 1 * *}", zone = "${ranking.cron.zone:Asia/Seoul}")
    @Transactional(readOnly = true)
    public void snapshotMonthlyTeamMemberCounts() {
        log.info("[snapshot] snapshotMonthlyTeamMemberCounts start");
        var now = ZonedDateTime.now(clock).withZoneSameInstant(keys.zoneId());
        var monthly = keys.monthlySuffix(now);
        var hashKey = keys.teamMemberCountMonthlyHash(monthly);

        var teams = teamRepository.findAll();
        if (teams.isEmpty()) {
            log.info("[snapshot] no teams for {}", monthly);
            return;
        }

        Map<Object, Object> map = new HashMap<>(teams.size());
        for (Team t : teams) {
            int count = Math.max(1, t.getTeamCurrentMembers());
            map.put(Long.toString(t.getTeamId()), Integer.toString(count));
        }

        redisTemplate.opsForHash().putAll(hashKey, map);
        log.info("[snapshot] saved team member counts for {} ({} teams)", monthly, map.size());
    }

    // =========================================================
    // 월간 아카이브(지난달)
    // =========================================================

    /**
     * [매월 1일 00:05 (기본)] 지난달의 개인/팀간 월간 랭킹 아카이빙.
     */
    @Scheduled(cron = "${ranking.cron.archive.monthly:0 5 0 1 * *}", zone = "${ranking.cron.zone:Asia/Seoul}")
    @Transactional
    public void archiveMonthlyRankings() {
        log.info("[archive-monthly] start");
        var now = ZonedDateTime.now(clock).withZoneSameInstant(keys.zoneId());
        var lastMonth = YearMonth.from(now).minusMonths(1);
        String ms = keys.monthlySuffix(lastMonth);

        int u = archiveMonthlyUserRanking(ms, lastMonth);
        int t = archiveMonthlyInterTeamRanking(ms, lastMonth);

        log.info("[archive-monthly] done: user={}, interTeam={}, month={}", u, t, ms);
    }

    /**
     * 지난달 개인 월간 랭킹을 아카이빙.
     * - 스냅샷 키로부터 점수 내림차순 페이지 처리
     * - 각 페이지를 변환 후 즉시 saveAll (메모리 절약)
     */
    private int archiveMonthlyUserRanking(String monthlySuffix, YearMonth lastMonth) {
        var rank = new AtomicLong(1);
        String sourceKey = keys.userMonthlyKey(monthlySuffix);
        String snapshotKey = keys.snapshotKey(sourceKey);

        if (!renameWithFallbackIfExists(sourceKey, snapshotKey)) {
            log.info("[archive user-monthly] no key: {}", sourceKey);
            return 0;
        }

        final int[] total = {0};
        try {
            processZSetInScoreOrder(snapshotKey, BATCH_SIZE, batch -> {
                var memberIds = batch.stream().map(t -> Long.parseLong(t.getValue())).toList();
                var memberMap = memberRepository.findAllById(memberIds).stream()
                    .collect(Collectors.toMap(Member::getMemberId, Function.identity()));

                List<MemberGlobalRanking> rows = new ArrayList<>(batch.size());
                for (var t : orderedDescending(batch)) {
                    var member = memberMap.get(Long.parseLong(t.getValue()));
                    if (member == null) continue;
                    long totalScore = toLongScore(t.getScore());
                    rows.add(new MemberGlobalRanking(
                        null,
                        lastMonth.getYear(),
                        lastMonth.getMonthValue(),
                        member,
                        (int) clipInt(totalScore),
                        (int) rank.getAndIncrement()
                    ));
                }

                if (!rows.isEmpty()) {
                    memberGlobalRankingRepository.saveAll(rows);
                    total[0] += rows.size();
                }
            });

            redisTemplate.delete(snapshotKey);
            return total[0];
        } catch (Exception e) {
            log.error("[archive user-monthly] FAILED. Restoring {}", snapshotKey, e);
            renameWithFallbackIfExists(snapshotKey, sourceKey); // 롤백 시도
            throw new RuntimeException("Failed to archive user-monthly", e);
        }
    }

    /**
     * 지난달 팀 간 월간 랭킹을 아카이빙.
     * - 평균 ZSET(필수), 합계 ZSET(선택)을 각각 스냅샷
     * - 평균 스냅샷이 없으면 중단(합계 스냅샷은 되돌림)
     * - 팀 멤버수는 월초 스냅샷(Hash)에서 우선 취득
     */
    private int archiveMonthlyInterTeamRanking(String monthlySuffix, YearMonth lastMonth) {
        String avgSrc = keys.interTeamMonthlyKey(monthlySuffix);
        String sumSrc = keys.teamScoreSumMonthlyKey(monthlySuffix);
        String avgSnap = keys.snapshotKey(avgSrc);
        String sumSnap = keys.snapshotKey(sumSrc);

        boolean hasAvg = renameWithFallbackIfExists(avgSrc, avgSnap);
        boolean hasSum = renameWithFallbackIfExists(sumSrc, sumSnap);

        if (!hasAvg) {
            log.info("[archive team-monthly] no avg key: {}", avgSrc);
            if (hasSum) renameWithFallbackIfExists(sumSnap, sumSrc); // 되돌림
            return 0;
        }

        try {
            // 합계 스냅샷을 Map(teamId -> totalScore)으로 로드
            Map<String, Double> totalScoreMap = new HashMap<>();
            if (hasSum) {
                var sumTuples = redisTemplate.opsForZSet().rangeWithScores(sumSnap, 0, -1);
                if (sumTuples != null) {
                    for (var t : sumTuples) totalScoreMap.put(t.getValue(), t.getScore());
                }
            }

            final int[] total = {0};
            processZSetInScoreOrder(avgSnap, BATCH_SIZE, batch -> {
                var teamIds = batch.stream().map(t -> Long.parseLong(t.getValue())).toList();
                var teamMap = teamRepository.findAllById(teamIds).stream()
                    .collect(Collectors.toMap(Team::getTeamId, Function.identity()));

                // 월초 스냅샷 Hash에서 멤버수 조회
                String hashKey = keys.teamMemberCountMonthlyHash(monthlySuffix);

                List<TeamGlobalRanking> rows = new ArrayList<>(batch.size());
                var rank = new AtomicLong(total[0] + 1L); // 누적 개수 기반 rank 시작값

                for (var t : orderedDescending(batch)) {
                    Long teamId = Long.parseLong(t.getValue());
                    Team team = teamMap.get(teamId);
                    if (team == null) continue;

                    double avg = (t.getScore() != null) ? t.getScore() : 0.0;
                    long totalScore = toLongScore(totalScoreMap.get(String.valueOf(teamId)));

                    // 스냅샷 멤버수 우선
                    int memberCount = getSnapshotMemberCountOrFallback(hashKey, teamId, team.getTeamCurrentMembers());

                    rows.add(new TeamGlobalRanking(
                        null,
                        lastMonth.getYear(),
                        lastMonth.getMonthValue(),
                        team,
                        (int) rank.getAndIncrement(),
                        BigDecimal.valueOf(avg),
                        totalScore,
                        memberCount
                    ));
                }

                if (!rows.isEmpty()) {
                    teamGlobalRankingRepository.saveAll(rows);
                    total[0] += rows.size();
                }
            });

            redisTemplate.delete(avgSnap);
            if (hasSum) redisTemplate.delete(sumSnap);

            return total[0];
        } catch (Exception e) {
            log.error("[archive team-monthly] FAILED. Restoring snapshots.", e);
            renameWithFallbackIfExists(avgSnap, avgSrc);
            if (hasSum) renameWithFallbackIfExists(sumSnap, sumSrc);
            throw new RuntimeException("Failed to archive team-monthly", e);
        }
    }

    // =========================================================
    // 주간 아카이브(지난주, 활성팀 Set 기반)
    // =========================================================

    /**
     * [매주 월요일 00:00 (기본)] 지난주에 활동이 있었던 모든 팀의 팀 내 주간 랭킹 아카이빙.
     * - 활성팀 SET: active_intra_teams:yyyy-ww
     */
    @Scheduled(cron = "${ranking.cron.archive.weekly:0 0 0 ? * MON}", zone = "${ranking.cron.zone:Asia/Seoul}")
    @Transactional
    public void archiveWeeklyIntraTeamRankings() {
        log.info("[archive-weekly] start");
        var now = ZonedDateTime.now(clock).withZoneSameInstant(keys.zoneId());
        var lastWeekDate = now.toLocalDate().minusWeeks(1);
        String weeklySuffix = keys.weeklySuffix(lastWeekDate);
        String activeKey = keys.activeIntraTeamWeeklyKey(weeklySuffix);

        var activeTeamIdsStr = redisTemplate.opsForSet().members(activeKey);
        if (activeTeamIdsStr == null || activeTeamIdsStr.isEmpty()) {
            log.info("[archive-weekly] no active teams set for {}", weeklySuffix);
            return;
        }

        var activeTeamIds = activeTeamIdsStr.stream().map(Long::parseLong).toList();
        var activeTeams = teamRepository.findAllById(activeTeamIds);
        int totalSaved = 0;

        for (Team team : activeTeams) {
            try {
                totalSaved += archiveIntraTeamRankingForTeam(team, weeklySuffix);
            } catch (Exception e) {
                log.error("[archive-weekly] FAILED for teamId {}. Skipping.", team.getTeamId(), e);
            }
        }

        redisTemplate.delete(activeKey);
        log.info("[archive-weekly] archived {}, cleared set: {}", totalSaved, activeKey);
    }

    /**
     * 특정 팀의 지난주 팀 내 랭킹을 아카이빙 (페이지 즉시 저장).
     */
    @Transactional
    public int archiveIntraTeamRankingForTeam(Team team, String weeklySuffix) {
        LocalDate monday = parseWeekSuffixToLocalDateISO(weeklySuffix);
        int year = monday.get(WF_ISO.weekBasedYear());
        int week = monday.get(WF_ISO.weekOfWeekBasedYear());

        String sourceKey = keys.intraTeamWeeklyKey(team.getTeamId(), weeklySuffix);
        String snapshotKey = keys.snapshotKey(sourceKey);

        if (!renameWithFallbackIfExists(sourceKey, snapshotKey)) {
            log.info("[archive intra-team] no key: {}", sourceKey);
            return 0;
        }

        final int[] total = {0};
        try {
            processZSetInScoreOrder(snapshotKey, BATCH_SIZE, batch -> {
                var memberIds = batch.stream().map(t -> Long.parseLong(t.getValue())).toList();
                var memberMap = memberRepository.findAllById(memberIds).stream()
                    .collect(Collectors.toMap(Member::getMemberId, Function.identity()));

                var rank = new AtomicLong(total[0] + 1L);
                List<MemberTeamRanking> rows = new ArrayList<>(batch.size());

                for (var t : orderedDescending(batch)) {
                    var member = memberMap.get(Long.parseLong(t.getValue()));
                    if (member == null) continue;

                    long totalScore = toLongScore(t.getScore());
                    rows.add(new MemberTeamRanking(
                        null, year, week, team, member,
                        (int) rank.getAndIncrement(),
                        (int) clipInt(totalScore)
                    ));
                }

                if (!rows.isEmpty()) {
                    memberTeamRankingRepository.saveAll(rows);
                    total[0] += rows.size();
                }
            });

            redisTemplate.delete(snapshotKey);
            return total[0];
        } catch (Exception e) {
            log.error("[archive intra-team] FAILED. Restoring {}", snapshotKey, e);
            renameWithFallbackIfExists(snapshotKey, sourceKey);
            throw new RuntimeException("Failed to archive intra-team weekly", e);
        }
    }

    // =========================================================
    // 공통 템플릿/도우미
    // =========================================================

    /**
     * 지정 ZSET을 점수 내림차순으로 페이지네이션 처리하여 콜백 수행.
     */
    private void processZSetInScoreOrder(String key, int pageSize, Consumer<List<TypedTuple<String>>> pageConsumer) {
        Long size = redisTemplate.opsForZSet().zCard(key);
        if (size == null || size == 0L) return;

        long total = size;
        for (long start = 0; start < total; start += pageSize) {
            long end = Math.min(total - 1, start + pageSize - 1);
            var page = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
            if (page == null || page.isEmpty()) break;

            // 명시적 정렬로 순서 안정화
            List<TypedTuple<String>> ordered = orderedDescending(page);
            pageConsumer.accept(ordered);
        }
    }

    private static List<TypedTuple<String>> orderedDescending(Iterable<TypedTuple<String>> tuples) {
        List<TypedTuple<String>> list = new ArrayList<>();
        for (var t : tuples) list.add(t);
        list.sort(Comparator.comparing(TypedTuple<String>::getScore, Comparator.nullsFirst(Double::compareTo)).reversed());
        return list;
    }

    /**
     * Redis 키를 스냅샷 키로 전환.
     * - 우선 RENAME 시도
     * - 실패(예: Redis Cluster CROSSSLOT) 시 COPY or DUMP/RESTORE + DEL 폴백
     * @return 원본 키가 존재하여 스냅샷이 준비되었으면 true, 아니면 false
     */
    private boolean renameWithFallbackIfExists(String from, String to) {
        return Boolean.TRUE.equals(redisTemplate.execute((RedisConnection conn) -> {
            byte[] f = from.getBytes(StandardCharsets.UTF_8);
            byte[] t = to.getBytes(StandardCharsets.UTF_8);
            if (!conn.keyCommands().exists(f)) return false;
            try {
                conn.keyCommands().rename(f, t);
                return true;
            } catch (Exception renameEx) {
                log.debug("[renameWithFallback] RENAME failed, try COPY or DUMP/RESTORE. from={} to={}", from, to, renameEx);
                try {
                    // Redis 6.2+ : COPY (REPLACE=true)
                    // 일부 Spring Data Redis 버전에서는 제공되지 않을 수 있음 → 예외 시 DUMP/RESTORE 폴백
                    conn.keyCommands().copy(f, t, true);
                    conn.keyCommands().del(f);
                    return true;
                } catch (Throwable copyEx) {
                    log.debug("[renameWithFallback] COPY failed, try DUMP/RESTORE. from={} to={}", from, to, copyEx);
                    try {
                        // 범용 폴백: DUMP -> RESTORE(REPLACE) -> DEL
                        byte[] dumped = conn.keyCommands().dump(f);
                        if (dumped == null) return false;
                        // 0 ms TTL, REPLACE 동작은 버전에 따라 다를 수 있으므로 먼저 DEL
                        conn.keyCommands().del(t);
                        conn.keyCommands().restore(t, 0L, dumped);
                        conn.keyCommands().del(f);
                        return true;
                    } catch (Throwable dumpRestoreEx) {
                        log.error("[renameWithFallback] DUMP/RESTORE failed. from={} to={}", from, to, dumpRestoreEx);
                        throw new DataAccessResourceFailureException("Unable to snapshot key: " + from, dumpRestoreEx);
                    }
                }
            }
        }));
    }

    private static long toLongScore(Double s) {
        return (s != null) ? Math.round(s) : 0L;
    }

    private static long clipInt(long value) {
        return Math.min(Integer.MAX_VALUE, Math.max(Integer.MIN_VALUE, value));
    }

    /**
     * "yyyy-ww"(ISO week) → 해당 주의 월요일(LocalDate) 반환.
     */
    private static LocalDate parseWeekSuffixToLocalDateISO(String weeklySuffix) {
        String[] parts = weeklySuffix.split("-");
        int year = Integer.parseInt(parts[0]);
        int week = Integer.parseInt(parts[1]);

        return LocalDate.of(year, 1, 4)
            .with(WF_ISO.weekBasedYear(), year)
            .with(WF_ISO.weekOfWeekBasedYear(), week)
            .with(WF_ISO.dayOfWeek(), 1);
    }

    /**
     * 월초 스냅샷 Hash에서 팀 멤버수를 우선 조회하고, 없으면 현재값으로 폴백.
     */
    private int getSnapshotMemberCountOrFallback(String hashKey, Long teamId, int currentMembers) {
        Object v = redisTemplate.opsForHash().get(hashKey, String.valueOf(teamId));
        if (v != null) {
            try {
                return Math.max(1, Integer.parseInt(v.toString()));
            } catch (NumberFormatException ignore) {
                // fall through
            }
        }
        return Math.max(1, currentMembers);
    }
}