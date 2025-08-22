package com.nyam.everyday.module.ranking.service;

import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.ranking.repository.MemberGlobalRankingRepository;
import com.nyam.everyday.module.ranking.repository.MemberTeamRankingRepository;
import com.nyam.everyday.module.ranking.repository.TeamGlobalRankingRepository;
import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.module.team.enums.ParticipationStatus;
import com.nyam.everyday.module.team.repository.TeamMemberStatusRepository;
import com.nyam.everyday.module.team.repository.TeamRepository;
import com.nyam.everyday.web.ranking.dto.RankingDto;
import com.nyam.everyday.web.ranking.dto.TeamRankingDto;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 실시간 랭킹 조회 및 점수 업데이트 서비스.
 * - 개인 월간
 * - 팀 간 월간 (평균 = 월초 스냅샷 멤버수 기반)
 * - 팀 내 주간 (ISO week)
 * 변경 사항 요약:
 * 1) ISO 주차 계산 일원화 (previousIsoWeek)
 * 2) updateMemberScore 파이프라인 2단계 적용 (RTT 감소)
 * 3) 팀 평균 계산 시 월초 멤버수 스냅샷 Hash 우선 사용
 * 4) rankDelta 계산시 DB 우선 → Redis 폴백 유지
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class RankingService {

  private final RedisTemplate<String, String> redisTemplate;
  private final MemberRepository memberRepository;
  private final TeamRepository teamRepository;
  private final TeamMemberStatusRepository teamMemberStatusRepository;
  private final MemberGlobalRankingRepository memberGlobalRankingRepository;
  private final TeamGlobalRankingRepository teamGlobalRankingRepository;
  private final MemberTeamRankingRepository memberTeamRankingRepository;
  private final RankingKeys keys;
  private final Clock clock;

  public RankingService(
      @Qualifier("redisRankingTemplate") RedisTemplate<String, String> redisTemplate,
      MemberRepository memberRepository,
      TeamRepository teamRepository,
      TeamMemberStatusRepository teamMemberStatusRepository,
      MemberGlobalRankingRepository memberGlobalRankingRepository,
      TeamGlobalRankingRepository teamGlobalRankingRepository,
      MemberTeamRankingRepository memberTeamRankingRepository,
      RankingKeys keys,
      Clock clock) {
    this.redisTemplate = redisTemplate;
    this.memberRepository = memberRepository;
    this.teamRepository = teamRepository;
    this.teamMemberStatusRepository = teamMemberStatusRepository;
    this.memberGlobalRankingRepository = memberGlobalRankingRepository;
    this.teamGlobalRankingRepository = teamGlobalRankingRepository;
    this.memberTeamRankingRepository = memberTeamRankingRepository;
    this.keys = keys;
    this.clock = clock;
  }

  // =========================================================
  // 쓰기 API
  // =========================================================

  /**
   * 특정 시각(KST) 기준으로 멤버 점수 업데이트.
   * - 개인 월간 ZINCRBY
   * - 각 팀에 대해: 팀내 주간 ZINCRBY, 활성팀 SADD, 팀 월간 합계 ZINCRBY
   * - 팀 평균 ZADD 는 2단계 파이프라인으로 일괄 반영
   */
  @Transactional
  public void updateMemberScore(Long memberId, long scoreToAdd, ZonedDateTime eventTimeKST) {
    Objects.requireNonNull(memberId, "memberId");
    if (scoreToAdd == 0L) return;

    final String monthly = keys.monthlySuffix(eventTimeKST);
    final String weekly  = keys.weeklySuffix(eventTimeKST);

    // 개인 월간은 항상, 팀 관련은 소속된 승인 팀에 대해서만
    List<Team> approvedTeams = findAllApprovedTeams(memberId);

    // 1단계 파이프라인: 개인/팀내/팀합계 증가, 활성팀 추가
    final String userMonthlyKey = keys.userMonthlyKey(monthly);
    final String activeKey      = keys.activeIntraTeamWeeklyKey(weekly);
    final String teamSumKey     = keys.teamScoreSumMonthlyKey(monthly);

    final List<Long> teamIds = approvedTeams.stream()
        .map(Team::getTeamId)
        .toList();

    final int teamCount = teamIds.size();

    SessionCallback<Object> incrAndMarkActive = new SessionCallback<>() {
      @Override
      public Object execute(RedisOperations operations) {
        @SuppressWarnings("unchecked")
        ZSetOperations<String, String> z = operations.opsForZSet();

        // 개인 월간 증가
        operations.opsForZSet().incrementScore(userMonthlyKey, String.valueOf(memberId), scoreToAdd);

        // 팀별 처리
        for (Long teamId : teamIds) {
          String memberStr = String.valueOf(memberId);
          String teamStr   = String.valueOf(teamId);

          // 팀내 주간 증가
          z.incrementScore(keys.intraTeamWeeklyKey(teamId, weekly), memberStr, scoreToAdd);
          // 활성팀 SET 기록
          operations.opsForSet().add(activeKey, teamStr);
          // 팀 월간 합계 증가
          z.incrementScore(teamSumKey, teamStr, scoreToAdd);
        }
        return null; // 파이프라인에서는 반환값을 사용하지 않음
      }
    };

    List<Object> pipeResults = redisTemplate.executePipelined(incrAndMarkActive);
    if (pipeResults == null) {
      throw new DataAccessResourceFailureException("Redis pipeline returned null");
    }
    // 팀이 없으면(개인 월간만 올리고) 바로 종료
    if (teamCount == 0) {
      return;
    }

    // 1 + 3 * teamCount 개의 결과가 오며,
    // index 0 = userMonthly ZINCRBY
    // 각 팀 i(0-based)에 대해:
    //   1 + 3*i = intraTeam ZINCRBY
    //   2 + 3*i = SADD
    //   3 + 3*i = teamSum ZINCRBY (필요)
    // 여기서 teamSum의 새로운 total을 이용해 평균 계산 후 interTeamMonthlyKey에 ZADD

    // 평균 반영은 2단계 파이프라인으로 묶어 RTT를 줄인다.
    final String interTeamKey = keys.interTeamMonthlyKey(monthly);
    final String memberCountHash = keys.teamMemberCountMonthlyHash(monthly);
    final HashOperations<String, Object, Object> h = redisTemplate.opsForHash();

// [추가] 월초 멤버수 스냅샷을 HMGET으로 한 번에 미리 로드
    List<Object> rawCounts = h.multiGet(
        memberCountHash,
        teamIds.stream().map(String::valueOf).collect(java.util.stream.Collectors.toList())
    );
    Map<Long, Integer> memberCountMap = new HashMap<>();
    // approvedTeams 를 map 으로 만들어 폴백에 활용
    Map<Long, Team> approvedTeamMap = approvedTeams.stream()
        .collect(Collectors.toMap(Team::getTeamId, Function.identity()));

    for (int i = 0; i < teamCount; i++) {
      Long teamId = teamIds.get(i);
      Object v = rawCounts.get(i);
      int cnt;
      try {
        cnt = (v != null) ? Math.max(1, Integer.parseInt(v.toString()))
            : Math.max(1, approvedTeamMap.get(teamId).getTeamCurrentMembers());
      } catch (NumberFormatException e) {
        cnt = Math.max(1, approvedTeamMap.get(teamId).getTeamCurrentMembers());
      }
      memberCountMap.put(teamId, cnt);
    }

  // 2단계 파이프라인: 평균 ZADD 일괄 반영 (읽기 없음)
    SessionCallback<Object> updateAverages = new SessionCallback<>() {
      @Override
      public Object execute(RedisOperations operations) {
        @SuppressWarnings("unchecked")
        ZSetOperations<String, String> z = operations.opsForZSet();

        for (int i = 0; i < teamCount; i++) {
          Long teamId = teamIds.get(i);
          int sumIndex = 3 + 3 * i; // ZINCRBY(team_sum) 결과 위치
          Double newTotal = castToDouble(pipeResults.get(sumIndex));
          long total = (newTotal != null) ? Math.round(newTotal) : 0L;

          int memberCount = memberCountMap.getOrDefault(teamId, 1);
          double average = (memberCount > 0) ? ((double) total / (double) memberCount) : 0.0;

          z.add(interTeamKey, String.valueOf(teamId), average);
        }
        return null;
      }
    };

    redisTemplate.executePipelined(updateAverages);
    redisTemplate.expire(activeKey, Duration.ofDays(21));
  }

  /** 현재 시각(KST) 기준 편의 메서드 */
  @Transactional
  public void updateMemberScore(Long memberId, long scoreToAdd) {
    updateMemberScore(memberId, scoreToAdd, ZonedDateTime.now(clock).withZoneSameInstant(keys.zoneId()));
  }

  // =========================================================
  // 조회 API
  // =========================================================

  /** 개인 월간 상위 N */
  public List<RankingDto> getTopRankers(int limit, Integer year, Integer month) {
    YearMonth ym = resolveMonth(year, month);
    String key = keys.userMonthlyKey(keys.monthlySuffix(ym));

    var tuples = rangeWithScoresSafe(key, 0, limit - 1);
    if (tuples.isEmpty()) return List.of();

    var memberIds = tuples.stream().map(t -> Long.parseLong(t.getValue())).toList();
    var memberMap = memberRepository.findAllById(memberIds).stream()
        .collect(Collectors.toMap(Member::getMemberId, Function.identity()));

    // 이전 달 순위를 배치 조회 (DB 우선 → Redis 폴백)
    YearMonth prev = ym.minusMonths(1);
    Map<Long, Integer> prevRankMap = loadPrevMemberRanksMonthly(prev, memberIds);

    long rank = 1;
    var list = new ArrayList<RankingDto>(tuples.size());
    for (var t : tuples) {
      long id = Long.parseLong(t.getValue());
      String nickname = Optional.ofNullable(memberMap.get(id)).map(Member::getNickname).orElse("Unknown");
      Integer delta = toDelta(prevRankMap.get(id), rank);
      list.add(new RankingDto(id, nickname, t.getScore(), rank++, delta));
    }
    return list;
  }

  /** 팀 간 월간 상위 N (평균 기준) */
  public List<TeamRankingDto> getInterTeamRanking(int limit, Integer year, Integer month) {
    YearMonth ym = resolveMonth(year, month);
    String key = keys.interTeamMonthlyKey(keys.monthlySuffix(ym));

    var tuples = rangeWithScoresSafe(key, 0, limit - 1);
    if (tuples.isEmpty()) return List.of();

    var teamIds = tuples.stream().map(t -> Long.parseLong(t.getValue())).toList();
    var teamMap = teamRepository.findAllById(teamIds).stream()
        .collect(Collectors.toMap(Team::getTeamId, Function.identity()));

    YearMonth prev = ym.minusMonths(1);
    Map<Long, Integer> prevRankMap = loadPrevTeamRanksMonthly(prev, teamIds);

    long rank = 1;
    var list = new ArrayList<TeamRankingDto>(tuples.size());
    for (var t : tuples) {
      long teamId = Long.parseLong(t.getValue());
      String teamName = Optional.ofNullable(teamMap.get(teamId)).map(Team::getTeamTitle).orElse("Unknown Team");
      Integer delta = toDelta(prevRankMap.get(teamId), rank);
      list.add(new TeamRankingDto(teamId, teamName, t.getScore(), rank++, delta));
    }
    return list;
  }

  /** 팀 내 주간 상위 N (ISO 주차) */
  public List<RankingDto> getIntraTeamRanking(Long teamId, int limit, Integer year, Integer week) {
    int[] yw = resolveWeek(year, week);
    int curYear = yw[0], curWeek = yw[1];
    String weeklySuffix = String.format("%d-%02d", curYear, curWeek);
    String key = keys.intraTeamWeeklyKey(teamId, weeklySuffix);

    var tuples = rangeWithScoresSafe(key, 0, limit - 1);
    if (tuples.isEmpty()) return List.of();

    var memberIds = tuples.stream().map(t -> Long.parseLong(t.getValue())).toList();
    var memberMap = memberRepository.findAllById(memberIds).stream()
        .collect(Collectors.toMap(Member::getMemberId, Function.identity()));

    int[] prevYW = previousIsoWeek(curYear, curWeek);
    Map<Long, Integer> prevRankMap = loadPrevMemberRanksWeekly(teamId, prevYW[0], prevYW[1], memberIds);

    long rank = 1;
    var list = new ArrayList<RankingDto>(tuples.size());
    for (var t : tuples) {
      long id = Long.parseLong(t.getValue());
      String nickname = Optional.ofNullable(memberMap.get(id)).map(Member::getNickname).orElse("Unknown");
      Integer delta = toDelta(prevRankMap.get(id), rank);
      list.add(new RankingDto(id, nickname, t.getScore(), rank++, delta));
    }
    return list;
  }

  /** 특정 멤버의 현재 월간 랭킹 단건 조회 */
  public RankingDto getMemberRank(Long memberId, Integer year, Integer month) {
    YearMonth ym = resolveMonth(year, month);
    String key = keys.userMonthlyKey(keys.monthlySuffix(ym));

    var z = redisTemplate.opsForZSet();
    Double score = z.score(key, String.valueOf(memberId));
    Long r0 = z.reverseRank(key, String.valueOf(memberId));
    Long currentRank = (r0 == null) ? null : (r0 + 1);

    YearMonth prev = ym.minusMonths(1);
    Integer prevRank = loadPrevMemberRankMonthly(prev, memberId);

    Integer delta = (currentRank == null) ? null : toDelta(prevRank, currentRank);
    return memberRepository.findById(memberId)
        .map(m -> new RankingDto(memberId, m.getNickname(), score, currentRank, delta))
        .orElse(null);
  }

  // =========================================================
  // 이전 순위 로더 (DB 우선 → Redis 폴백)
  // =========================================================

  private Map<Long, Integer> loadPrevMemberRanksMonthly(YearMonth prev, Collection<Long> memberIds) {
    if (memberIds.isEmpty()) return Map.of();
    Map<Long, Integer> map = memberGlobalRankingRepository
        .findRanksByYearMonthAndMemberIds(prev.getYear(), prev.getMonthValue(), memberIds)
        .stream().collect(Collectors.toMap(
            row -> (Long) row[0],
            row -> (Integer) row[1]
        ));
    if (map.size() == memberIds.size()) return map;

    String prevKey = keys.userMonthlyKey(keys.monthlySuffix(prev));
    for (Long id : memberIds) {
      if (map.containsKey(id)) continue;
      Long r0 = redisTemplate.opsForZSet().reverseRank(prevKey, String.valueOf(id));
      if (r0 != null) map.put(id, (int) (r0 + 1));
    }
    return map;
  }

  private Integer loadPrevMemberRankMonthly(YearMonth prev, Long memberId) {
    return memberGlobalRankingRepository
        .findRankByYearMonthAndMemberId(prev.getYear(), prev.getMonthValue(), memberId)
        .orElseGet(() -> {
          String prevKey = keys.userMonthlyKey(keys.monthlySuffix(prev));
          Long r0 = redisTemplate.opsForZSet().reverseRank(prevKey, String.valueOf(memberId));
          return (r0 == null) ? null : (int) (r0 + 1);
        });
  }

  private Map<Long, Integer> loadPrevTeamRanksMonthly(YearMonth prev, Collection<Long> teamIds) {
    if (teamIds.isEmpty()) return Map.of();
    Map<Long, Integer> map = teamGlobalRankingRepository
        .findRanksByYearMonthAndTeamIds(prev.getYear(), prev.getMonthValue(), teamIds)
        .stream().collect(Collectors.toMap(
            row -> (Long) row[0],
            row -> (Integer) row[1]
        ));
    if (map.size() == teamIds.size()) return map;

    String prevKey = keys.interTeamMonthlyKey(keys.monthlySuffix(prev));
    for (Long id : teamIds) {
      if (map.containsKey(id)) continue;
      Long r0 = redisTemplate.opsForZSet().reverseRank(prevKey, String.valueOf(id));
      if (r0 != null) map.put(id, (int) (r0 + 1));
    }
    return map;
  }

  private Map<Long, Integer> loadPrevMemberRanksWeekly(Long teamId, int year, int week, Collection<Long> memberIds) {
    if (memberIds.isEmpty()) return Map.of();
    Map<Long, Integer> map = memberTeamRankingRepository
        .findRanksByYearWeekTeamAndMemberIds(year, week, teamId, memberIds)
        .stream().collect(Collectors.toMap(
            row -> (Long) row[0],
            row -> (Integer) row[1]
        ));
    if (map.size() == memberIds.size()) return map;

    String prevKey = keys.intraTeamWeeklyKey(teamId, String.format("%d-%02d", year, week));
    for (Long id : memberIds) {
      if (map.containsKey(id)) continue;
      Long r0 = redisTemplate.opsForZSet().reverseRank(prevKey, String.valueOf(id));
      if (r0 != null) map.put(id, (int) (r0 + 1));
    }
    return map;
  }

  // =========================================================
  // 내부 유틸
  // =========================================================

  /** ISO week 기준: (year, week) → (previousYear, previousWeek) */
  private int[] previousIsoWeek(int year, int week) {
    WeekFields wf = WeekFields.ISO;
    LocalDate anchor = LocalDate.of(year, 1, 4)
        .with(wf.weekBasedYear(), year)
        .with(wf.weekOfWeekBasedYear(), week)
        .with(wf.dayOfWeek(), 1);
    LocalDate prev = anchor.minusWeeks(1);
    return new int[]{ prev.get(wf.weekBasedYear()), prev.get(wf.weekOfWeekBasedYear()) };
  }

  private YearMonth resolveMonth(Integer year, Integer month) {
    if (year != null && month != null) return YearMonth.of(year, month);
    return YearMonth.from(ZonedDateTime.now(clock).withZoneSameInstant(keys.zoneId()));
  }

  private int[] resolveWeek(Integer year, Integer week) {
    if (year != null && week != null) return new int[]{year, week};
    var now = ZonedDateTime.now(clock).withZoneSameInstant(keys.zoneId()).toLocalDate();
    var wf = WeekFields.ISO;
    return new int[]{ now.get(wf.weekBasedYear()), now.get(wf.weekOfWeekBasedYear()) };
  }

  private Integer toDelta(Integer previousRank, long currentRank) {
    return (previousRank == null) ? null : previousRank - (int) currentRank;
  }

  /** 안전한 reverseRangeWithScores (null → empty) */
  private Set<TypedTuple<String>> rangeWithScoresSafe(String key, long start, long end) {
    var r = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    return (r != null) ? r : Collections.emptySet();
  }

  /** 승인된 팀 소속 전체 조회 */
  private List<Team> findAllApprovedTeams(Long memberId) {
    return teamMemberStatusRepository.getAllByMember_MemberId(memberId)
        .stream()
        .filter(s -> s.getStatus() == ParticipationStatus.APPROVED)
        .map(TeamMemberStatus::getTeam)
        .collect(Collectors.toList());
  }

  /** 월초 스냅샷 멤버수(Hash) 우선, 없으면 현재 멤버수 폴백 */
  private int getTeamMemberCountSnapshotOrFallback(HashOperations<String, Object, Object> h, String hashKey, Long teamId) {
    Object v = h.get(hashKey, String.valueOf(teamId));
    if (v != null) {
      try {
        return Math.max(1, Integer.parseInt(v.toString()));
      } catch (NumberFormatException ignore) { /* fall-through */ }
    }
    // DB 현재값 폴백
    return teamRepository.findById(teamId)
        .map(Team::getTeamCurrentMembers)
        .map(cnt -> Math.max(1, cnt))
        .orElse(1);
  }

  private static Double castToDouble(Object o) {
    if (o == null) return null;
    if (o instanceof Double d) return d;
    if (o instanceof Number n) return n.doubleValue();
    throw new DataAccessResourceFailureException("Unexpected pipeline result type: " + o.getClass());
  }

  // =========================================================
  // 운영 유틸(키 초기화)
  // =========================================================

  @Transactional
  public void clearMemberRanking(Integer year, Integer month) {
    String k = keys.userMonthlyKey(String.format("%d-%02d", year, month));
    redisTemplate.delete(k);
    log.info("Cleared member ranking for key: {}", k);
  }

  @Transactional
  public void clearInterTeamRanking(Integer year, Integer month) {
    String ms = String.format("%d-%02d", year, month);
    String interKey = keys.interTeamMonthlyKey(ms);
    String sumKey = keys.teamScoreSumMonthlyKey(ms);
    redisTemplate.delete(interKey);
    redisTemplate.delete(sumKey);
    log.info("Cleared inter-team ranking: {}, team score sum: {}", interKey, sumKey);
  }

  @Transactional
  public void clearIntraTeamRanking(Long teamId, Integer year, Integer week) {
    String ws = String.format("%d-%02d", year, week);
    String k = keys.intraTeamWeeklyKey(teamId, ws);
    redisTemplate.delete(k);
    log.info("Cleared intra-team ranking for key: {}", k);
  }
}