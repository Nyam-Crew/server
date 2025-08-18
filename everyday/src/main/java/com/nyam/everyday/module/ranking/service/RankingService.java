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
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RankingService {

  private final RedisTemplate<String, String> redisTemplate;
  private final MemberRepository memberRepository;
  private final TeamRepository teamRepository;
  private final TeamMemberStatusRepository teamMemberStatusRepository;

  private static final String USER_RANKING_KEY_PREFIX = "user_ranking:";
  private static final String INTRA_TEAM_RANKING_KEY_PREFIX = "team_ranking:";
  private static final String INTER_TEAM_RANKING_KEY_PREFIX = "inter_team_ranking:";
  private static final String TEAM_SCORE_SUM_KEY_PREFIX = "team_score_sum:";

  /**
   * 사용자의 점수를 업데이트하고, 관련된 모든 랭킹(개인, 팀 내부, 팀 간)을 갱신합니다.
   *
   * @param memberId   점수를 업데이트할 회원의 ID
   * @param scoreToAdd 추가할 점수
   */
  @Transactional
  public void updateMemberScore(Long memberId, Long scoreToAdd) {
    String dateSuffix = getDateSuffix(null, null);

    // 1. 개인 랭킹 업데이트
    redisTemplate.opsForZSet().incrementScore(USER_RANKING_KEY_PREFIX + dateSuffix, memberId.toString(), scoreToAdd);

    // 2. 팀 랭킹 업데이트 (소속된 팀이 있는 경우)
    findCurrentTeam(memberId).ifPresent(team -> {
      Long teamId = team.getTeamId();

      // 2-1. 팀 내부 멤버 랭킹 업데이트
      redisTemplate.opsForZSet().incrementScore(getIntraTeamRankingKey(teamId, dateSuffix), memberId.toString(), scoreToAdd);

      // 2-2. 팀 총점 업데이트
      Double newTotalScore = redisTemplate.opsForZSet().incrementScore(TEAM_SCORE_SUM_KEY_PREFIX + dateSuffix, teamId.toString(), scoreToAdd);

      // 2-3. 팀 평균 점수 계산 및 팀 간 랭킹 업데이트
      int memberCount = team.getTeamCurrentMembers();
      if (memberCount > 0 && newTotalScore != null) {
        double averageScore = newTotalScore / memberCount;
        redisTemplate.opsForZSet().add(INTER_TEAM_RANKING_KEY_PREFIX + dateSuffix, teamId.toString(), averageScore);
      }
    });
  }

  /**
   * 전체 개인 랭킹 상위 N명의 목록을 조회합니다.
   *
   * @param limit 조회할 랭커의 수
   * @param year  조회할 년도 (지정하지 않으면 현재)
   * @param month 조회할 월 (지정하지 않으면 현재)
   * @return 개인 랭킹 정보 DTO 리스트
   */
  public List<RankingDto> getTopRankers(int limit, Integer year, Integer month) {
    String key = getRankingKey(year, month);
    return getMemberRankingFromKey(key, limit);
  }

  /**
   * 팀 간 랭킹 상위 N팀의 목록을 조회합니다. (팀 평균 점수 기준)
   *
   * @param limit 조회할 팀의 수
   * @param year  조회할 년도 (지정하지 않으면 현재)
   * @param month 조회할 월 (지정하지 않으면 현재)
   * @return 팀 랭킹 정보 DTO 리스트
   */
  public List<TeamRankingDto> getInterTeamRanking(int limit, Integer year, Integer month) {
    String key = getInterTeamRankingKey(year, month);
    Set<ZSetOperations.TypedTuple<String>> typedTuples = getTopTuplesFromRedis(key, limit);

    if (typedTuples.isEmpty()) {
      return Collections.emptyList();
    }

    //팀 ID 목록으로 팀 정보 한 번에 조회
    List<Long> teamIds = typedTuples.stream().map(tuple -> Long.parseLong(tuple.getValue())).collect(Collectors.toList());
    Map<Long, Team> teamMap = teamRepository.findAllById(teamIds).stream()
        .collect(Collectors.toMap(Team::getTeamId, Function.identity()));

    long rank = 1;
    List<TeamRankingDto> topTeams = new ArrayList<>();
    for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
      Long teamId = Long.parseLong(tuple.getValue());
      Team team = teamMap.get(teamId);
      String teamName = (team != null) ? team.getTeamTitle() : "Unknown Team";

      topTeams.add(TeamRankingDto.builder()
          .teamId(teamId)
          .teamName(teamName)
          .averageScore(tuple.getScore())
          .rank(rank++)
          .build());
    }
    return topTeams;
  }

  /**
   * 특정 팀 내부의 멤버 랭킹 상위 N명의 목록을 조회합니다. (개인 점수 기준)
   *
   * @param teamId  조회할 팀의 ID
   * @param limit   조회할 랭커의 수
   * @param year    조회할 년도 (지정하지 않으면 현재)
   * @param month   조회할 월 (지정하지 않으면 현재)
   * @return 개인 랭킹 정보 DTO 리스트
   */
  public List<RankingDto> getIntraTeamRanking(Long teamId, int limit, Integer year, Integer month) {
    String key = getIntraTeamRankingKey(teamId, getDateSuffix(year, month));
    return getMemberRankingFromKey(key, limit);
  }

  /**
   * 특정 사용자의 개인 랭킹 정보를 조회합니다.
   *
   * @param memberId 조회할 회원의 ID
   * @param year     조회할 년도 (지정하지 않으면 현재)
   * @param month    조회할 월 (지정하지 않으면 현재)
   * @return 사용자의 랭킹 정보 DTO
   */
  public RankingDto getUserRank(Long memberId, Integer year, Integer month) {
    String key = getRankingKey(year, month);
    ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();

    Double score = zSetOperations.score(key, memberId.toString());
    Long rank = zSetOperations.reverseRank(key, memberId.toString());

    return memberRepository.findById(memberId)
        .map(member -> RankingDto.builder()
            .memberId(memberId)
            .nickname(member.getNickname())
            .totalScore(score)
            .rank(rank != null ? rank + 1 : null)
            .build())
        .orElse(null); // 또는 예외 처리
  }

  /**
   * 지정된 월의 개인 랭킹을 초기화(삭제)합니다.
   *
   * @param year  초기화할 년도
   * @param month 초기화할 월
   */
  public void clearUserRanking(Integer year, Integer month) {
    String key = getRankingKey(year, month);
    redisTemplate.delete(key);
    log.info("Cleared user ranking for key: {}", key);
  }

  /**
   * 지정된 월의 팀 간 랭킹을 초기화(삭제)합니다.
   * 팀 평균 점수 랭킹과 팀 총점 데이터가 함께 삭제됩니다.
   *
   * @param year  초기화할 년도
   * @param month 초기화할 월
   */
  public void clearInterTeamRanking(Integer year, Integer month) {
    String interTeamKey = getInterTeamRankingKey(year, month);
    redisTemplate.delete(interTeamKey);
    log.info("Cleared inter-team ranking for key: {}", interTeamKey);

    String teamScoreSumKey = getTeamScoreSumKey(year, month);
    redisTemplate.delete(teamScoreSumKey);
    log.info("Cleared team score sum for key: {}", teamScoreSumKey);
  }

  /**
   * 지정된 월의 특정 팀 내부 랭킹을 초기화(삭제)합니다.
   *
   * @param teamId 초기화할 팀의 ID
   * @param year   초기화할 년도
   * @param month  초기화할 월
   */
  public void clearIntraTeamRanking(Long teamId, Integer year, Integer month) {
    String key = getIntraTeamRankingKey(teamId, getDateSuffix(year, month));
    redisTemplate.delete(key);
    log.info("Cleared intra-team ranking for key: {}", key);
  }

  private List<RankingDto> getMemberRankingFromKey(String key, int limit) {
    Set<ZSetOperations.TypedTuple<String>> typedTuples = getTopTuplesFromRedis(key, limit);

    if (typedTuples.isEmpty()) {
      return Collections.emptyList();
    }

    // 멤버 ID 목록으로 멤버 정보 한 번에 조회
    List<Long> memberIds = typedTuples.stream().map(tuple -> Long.parseLong(tuple.getValue())).collect(Collectors.toList());
    Map<Long, Member> memberMap = memberRepository.findAllById(memberIds).stream()
        .collect(Collectors.toMap(Member::getMemberId, Function.identity()));

    long rank = 1;
    List<RankingDto> topRankers = new ArrayList<>();
    for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
      log.info("ranking Info : {}" , tuple.getValue() + " : " + tuple.getScore() );
      Long memberId = Long.parseLong(tuple.getValue());
      Member member = memberMap.get(memberId);
      String nickname = (member != null) ? member.getNickname() : "Unknown";

      topRankers.add(RankingDto.builder()
          .memberId(memberId)
          .nickname(nickname)
          .totalScore(tuple.getScore())
          .rank(rank++)
          .build());
    }
    return topRankers;
  }

  private Set<ZSetOperations.TypedTuple<String>> getTopTuplesFromRedis(String key, int limit) {
    ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
    return zSetOperations.reverseRangeWithScores(key, 0, limit - 1);
  }

  private Optional<Team> findCurrentTeam(Long memberId) {
    return teamMemberStatusRepository.getAllByMember_MemberId(memberId)
        .stream()
        .filter(status -> status.getStatus() == ParticipationStatus.APPROVED)
        .findFirst()
        .map(TeamMemberStatus::getTeam);
  }

  private String getRankingKey(Integer year, Integer month) {
    return USER_RANKING_KEY_PREFIX + getDateSuffix(year, month);
  }

  private String getInterTeamRankingKey(Integer year, Integer month) {
    return INTER_TEAM_RANKING_KEY_PREFIX + getDateSuffix(year, month);
  }

  private String getIntraTeamRankingKey(Long teamId, String dateSuffix) {
    return INTRA_TEAM_RANKING_KEY_PREFIX + teamId + ":" + dateSuffix;
  }

  private String getTeamScoreSumKey(Integer year, Integer month) {
    return TEAM_SCORE_SUM_KEY_PREFIX + getDateSuffix(year, month);
  }

  private String getDateSuffix(Integer year, Integer month) {
    return (year != null && month != null)
        ? String.format("%d-%02d", year, month)
        : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
  }
}
