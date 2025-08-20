package com.nyam.everyday.module.ranking.service;

import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.module.team.enums.ParticipationStatus;
import com.nyam.everyday.module.team.repository.TeamMemberStatusRepository;
import com.nyam.everyday.module.team.repository.TeamRepository;
import com.nyam.everyday.web.ranking.dto.RankingDto;
import com.nyam.everyday.web.ranking.dto.TeamRankingDto;
import java.time.Clock;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 실시간 랭킹 조회 및 점수 업데이트를 담당하는 서비스입니다.
 * - 개인 랭킹 (월간)
 * - 팀 간 랭킹 (월간)
 * - 팀 내부 랭킹 (주간)
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class RankingService {

  private final RedisTemplate<String, String> redisTemplate;
  private final MemberRepository memberRepository;
  private final TeamRepository teamRepository;
  private final TeamMemberStatusRepository teamMemberStatusRepository;
  private final RankingKeys keys;

  private final Clock clock = Clock.systemDefaultZone();

    public RankingService(@Qualifier("redisRankingTemplate") RedisTemplate<String, String> redisTemplate, MemberRepository memberRepository, TeamRepository teamRepository, TeamMemberStatusRepository teamMemberStatusRepository, RankingKeys keys) {
        this.redisTemplate = redisTemplate;
        this.memberRepository = memberRepository;
        this.teamRepository = teamRepository;
        this.teamMemberStatusRepository = teamMemberStatusRepository;
        this.keys = keys;
    }

    // ========== 쓰기 API ========== 

  /**
   * 특정 시각을 기준으로 멤버의 점수를 업데이트합니다. 개인/팀내/팀간 랭킹을 모두 갱신합니다.
   * @param memberId 점수를 부여할 멤버 ID
   * @param scoreToAdd 추가할 점수
   * @param eventTimeKST 이벤트 발생 시각 (KST)
   */
  @Transactional
  public void updateMemberScore(Long memberId, long scoreToAdd, ZonedDateTime eventTimeKST) {
    ZSetOperations<String, String> z = redisTemplate.opsForZSet();
    HashOperations<String, Object, Object> h = redisTemplate.opsForHash();

    String monthly = keys.monthlySuffix(eventTimeKST);
    String weekly  = keys.weeklySuffix(eventTimeKST);

    // 1) 개인 월간
    z.incrementScore(keys.userMonthlyKey(monthly), String.valueOf(memberId), scoreToAdd);

    // 2) 팀 소속이 있으면 팀내 주간 + 팀간 월간 업데이트
    findCurrentTeam(memberId).ifPresent(team -> {
      long teamId = team.getTeamId();
      var teamIdStr = Long.toString(teamId);

      // 2-1) 팀내 주간
      z.incrementScore(keys.intraTeamWeeklyKey(teamId, weekly), String.valueOf(memberId), scoreToAdd);

      // 2-1-1) 활성 팀 Set (TTL 부여)
      var activeKey = keys.activeIntraTeamWeeklyKey(weekly);
      redisTemplate.opsForSet().add(activeKey, teamIdStr);
      redisTemplate.expire(activeKey, Duration.ofDays(21));

      // 2-2) 팀 총점(월간)
      Double newTotal = z.incrementScore(keys.teamScoreSumMonthlyKey(monthly), teamIdStr, scoreToAdd);

      // 2-3) 팀 평균(월간): 멤버수 스냅샷 사용(없으면 fallback)
      int memberCount = getTeamMemberCountSnapshotOrFallback(h, monthly, team);
      double average = (newTotal != null && memberCount > 0) ? (newTotal / memberCount) : 0.0;
      z.add(keys.interTeamMonthlyKey(monthly), teamIdStr, average);
    });
  }

  /**
   * 현재 시각을 기준으로 멤버의 점수를 업데이트하는 편의 메소드입니다.
   * @param memberId 점수를 부여할 멤버 ID
   * @param scoreToAdd 추가할 점수
   */
  @Transactional
  public void updateMemberScore(Long memberId, long scoreToAdd) {
    updateMemberScore(memberId, scoreToAdd, ZonedDateTime.now(clock).withZoneSameInstant(keys.zoneId()));
  }

  // ========== 조회 API ========== 

  /**
   * 전체 개인 랭킹 상위 N개를 조회합니다.
   * @param limit 조회할 랭킹 수
   * @param year 조회할 년도 (null이면 현재)
   * @param month 조회할 월 (null이면 현재)
   * @return 랭킹 DTO 리스트
   */
  public List<RankingDto> getTopRankers(int limit, Integer year, Integer month) {
    YearMonth ym = (year != null && month != null)
        ? YearMonth.of(year, month)
        : YearMonth.from(ZonedDateTime.now(clock).withZoneSameInstant(keys.zoneId()));
    String key = keys.userMonthlyKey(keys.monthlySuffix(ym));
    return getMemberRankingFromKey(key, limit);
  }

  /**
   * 팀 간 월간 랭킹 상위 N개를 조회합니다.
   * @param limit 조회할 랭킹 수
   * @param year 조회할 년도 (null이면 현재)
   * @param month 조회할 월 (null이면 현재)
   * @return 팀 랭킹 DTO 리스트
   */
  public List<TeamRankingDto> getInterTeamRanking(int limit, Integer year, Integer month) {
    YearMonth ym = (year != null && month != null)
        ? YearMonth.of(year, month)
        : YearMonth.from(ZonedDateTime.now(clock).withZoneSameInstant(keys.zoneId()));
    String key = keys.interTeamMonthlyKey(keys.monthlySuffix(ym));

    var tuples = rangeWithScoresSafe(key, 0, limit - 1);
    if (tuples.isEmpty()) return Collections.emptyList();

    var teamIds = tuples.stream().map(t -> Long.parseLong(t.getValue())).toList();
    var teamMap = teamRepository.findAllById(teamIds).stream()
        .collect(Collectors.toMap(Team::getTeamId, Function.identity()));

    long rank = 1;
    var list = new ArrayList<TeamRankingDto>(tuples.size());
    for (var t : tuples) {
      var teamId = Long.parseLong(t.getValue());
      var team = teamMap.get(teamId);
      var teamName = (team != null) ? team.getTeamTitle() : "Unknown Team";
      list.add(new TeamRankingDto(teamId, teamName, t.getScore(), rank++));
    }
    return list;
  }

  /**
   * 특정 팀의 주간 팀 내 랭킹 상위 N개를 조회합니다.
   * @param teamId 조회할 팀 ID
   * @param limit 조회할 랭킹 수
   * @param year 조회할 년도 (null이면 현재)
   * @param week 조회할 주 (null이면 현재)
   * @return 랭킹 DTO 리스트
   */
  public List<RankingDto> getIntraTeamRanking(Long teamId, int limit, Integer year, Integer week) {
    String weeklySuffix = (year != null && week != null)
        ? String.format("%d-%02d", year, week)
        : keys.weeklySuffix(ZonedDateTime.now(clock).withZoneSameInstant(keys.zoneId()));
    String key = keys.intraTeamWeeklyKey(teamId, weeklySuffix);
    return getMemberRankingFromKey(key, limit);
  }

  /**
   * 특정 멤버의 월간 개인 랭킹 순위와 점수를 조회합니다.
   * @param memberId 조회할 멤버 ID
   * @param year 조회할 년도 (null이면 현재)
   * @param month 조회할 월 (null이면 현재)
   * @return 랭킹 DTO
   */
  public RankingDto getMemberRank(Long memberId, Integer year, Integer month) {
    YearMonth ym = (year != null && month != null)
        ? YearMonth.of(year, month)
        : YearMonth.from(ZonedDateTime.now(clock).withZoneSameInstant(keys.zoneId()));
    String key = keys.userMonthlyKey(keys.monthlySuffix(ym));

    var z = redisTemplate.opsForZSet();
    var score = z.score(key, String.valueOf(memberId));
    var rank0 = z.reverseRank(key, String.valueOf(memberId));

    return memberRepository.findById(memberId)
        .map(m -> new RankingDto(memberId, m.getNickname(), score, (rank0 != null ? rank0 + 1 : null)))
        .orElse(null);
  }

  // ========== 수동 초기화(운영도구) ========== 

  /**
   * (운영) 특정 월의 개인 랭킹 데이터를 삭제합니다.
   */
  @Transactional
  public void clearMemberRanking(Integer year, Integer month) {
    String k = keys.userMonthlyKey(String.format("%d-%02d", year, month));
    redisTemplate.delete(k);
    log.info("Cleared member ranking for key: {}", k);
  }

  /**
   * (운영) 특정 월의 팀 간 랭킹 데이터를 삭제합니다.
   */
  @Transactional
  public void clearInterTeamRanking(Integer year, Integer month) {
    String ms = String.format("%d-%02d", year, month);
    String interKey = keys.interTeamMonthlyKey(ms);
    String sumKey = keys.teamScoreSumMonthlyKey(ms);
    redisTemplate.delete(interKey);
    redisTemplate.delete(sumKey);
    log.info("Cleared inter-team ranking: {}, team score sum: {}", interKey, sumKey);
  }

  /**
   * (운영) 특정 팀의 특정 주간 랭킹 데이터를 삭제합니다.
   */
  @Transactional
  public void clearIntraTeamRanking(Long teamId, Integer year, Integer week) {
    String ws = String.format("%d-%02d", year, week);
    String k = keys.intraTeamWeeklyKey(teamId, ws);
    redisTemplate.delete(k);
    log.info("Cleared intra-team ranking for key: {}", k);
  }

  // ========== 내부 유틸 ========== 

  /** Redis ZSET 조회를 안전하게 처리하고 결과가 null일 경우 빈 Set을 반환합니다. */
  private Set<ZSetOperations.TypedTuple<String>> rangeWithScoresSafe(String key, long start, long end) {
    var r = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    return (r != null) ? r : Collections.emptySet();
  }

  /** Redis 키와 limit을 받아 멤버 정보를 포함한 랭킹 DTO 리스트를 생성합니다. */
  private List<RankingDto> getMemberRankingFromKey(String key, int limit) {
    var tuples = rangeWithScoresSafe(key, 0, limit - 1);
    if (tuples.isEmpty()) return Collections.emptyList();

    var memberIds = tuples.stream().map(t -> Long.parseLong(t.getValue())).toList();
    var memberMap = memberRepository.findAllById(memberIds).stream()
        .collect(Collectors.toMap(Member::getMemberId, Function.identity()));

    long rank = 1;
    var list = new ArrayList<RankingDto>(tuples.size());
    for (var t : tuples) {
      var id = Long.parseLong(t.getValue());
      var member = memberMap.get(id);
      var nickname = (member != null) ? member.getNickname() : "Unknown";
      list.add(new RankingDto(id, nickname, t.getScore(), rank++));
    }
    return list;
  }

  /** 특정 멤버가 현재 소속된(참여 승인된) 팀을 찾습니다. */
  private Optional<Team> findCurrentTeam(Long memberId) {
    return teamMemberStatusRepository.getAllByMember_MemberId(memberId)
        .stream()
        .filter(s -> s.getStatus() == ParticipationStatus.APPROVED)
        .findFirst()
        .map(TeamMemberStatus::getTeam);
  }

  /** 월 시작 시 저장된 멤버수 스냅샷을 우선 사용하고, 없으면(신규팀 등) DB의 현재 값을 사용합니다. */
  private int getTeamMemberCountSnapshotOrFallback(HashOperations<String, Object, Object> h, String monthlySuffix, Team team) {
    String hash = keys.teamMemberCountMonthlyHash(monthlySuffix);
    Object v = h.get(hash, Long.toString(team.getTeamId()));
    if (v != null) {
      try { return Integer.parseInt(v.toString()); } catch (NumberFormatException ignore) {}
    }
    return Math.max(1, team.getTeamCurrentMembers());
  }
}
