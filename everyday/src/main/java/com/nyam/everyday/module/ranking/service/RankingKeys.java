package com.nyam.everyday.module.ranking.service;

import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import org.springframework.stereotype.Component;

/**
 * 랭킹/통계 Redis 키 생성을 단일화합니다.
 * 설계 원칙
 * - 월간 suffix: yyyy-MM (예: 2025-08)
 * - 주간 suffix: yyyy-ww (ISO week-based-year 기준, 예: 2025-34)
 * - Redis Cluster 안전성: 기간 구분자(yyyy-MM, yyyy-ww)를 {해시태그}로 감싸 동일 슬롯 보장
 *   예) user_ranking:{2025-08}, team_ranking:{2025-34}:123
 * - 스냅샷 키는 "snapshot:" 접두사를 붙여도 {…} 슬롯 태그가 유지되어 RENAME 안전
 */
@Component
public class RankingKeys {

  // ---------------- Constants ----------------
  // 개인 월간 랭킹
  public static final String USER_RANKING_KEY_PREFIX        = "user_ranking:";         // ZSET(memberId -> totalScore) per month
  // 팀 내부 주간 랭킹
  public static final String INTRA_TEAM_RANKING_KEY_PREFIX  = "team_ranking:";         // ZSET(memberId -> score) per team/week
  // 팀간 월간 랭킹 (최종) - 평균점수 기준
  public static final String INTER_TEAM_RANKING_KEY_PREFIX  = "inter_team_ranking:";   // ZSET(teamId -> avgScore) per month
  // 최종 팀 랭킹(평균 점수)을 계산하기 위한 보조 데이터 - 팀 월간 점수 합계
  public static final String TEAM_SCORE_SUM_KEY_PREFIX      = "team_score_sum:";       // ZSET(teamId -> totalScore) per month
  // 보조 데이터 - 월초 팀 멤버 수
  public static final String TEAM_MEMBER_COUNT_HASH_PREFIX  = "team_member_count:";    // HASH(teamId -> count) per month
  // 주간 활성 팀 - 해당 주에 활동(점수 획득)이 있었던 팀을 기록
  public static final String ACTIVE_INTRA_TEAM_KEY_PREFIX   = "active_intra_teams:";   // SET(teamId) per week
  // 아카이빙을 위한 스냅샷 prefix (redis 키들을 스냅샷으로 변경 후 DB 저장 후 키 삭제)
  public static final String SNAPSHOT_PREFIX                = "snapshot:";             // rename 스냅샷 접두사

  private static final DateTimeFormatter YM_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
  private static final WeekFields WF_ISO = WeekFields.ISO;

  private final ZoneId zoneId = ZoneId.of("Asia/Seoul");

  // ---------------- Time/Zone ----------------
  public ZoneId zoneId() { return zoneId; }

  // ---------------- Suffix ----------------
  /** 월간 suffix: yyyy-MM */
  public String monthlySuffix(YearMonth ym) {
    return ym.format(YM_FMT);
  }

  /** 월간 suffix: yyyy-MM (KST 기준 ZonedDateTime 입력) */
  public String monthlySuffix(ZonedDateTime timeKst) {
    return YearMonth.from(timeKst).format(YM_FMT);
  }

  /** 주간 suffix: yyyy-ww (ISO week-based-year 기준) */
  public String weeklySuffix(LocalDate dateKst) {
    int week = dateKst.get(WF_ISO.weekOfWeekBasedYear());
    int year = dateKst.get(WF_ISO.weekBasedYear());
    return String.format("%d-%02d", year, week);
  }

  /** 주간 suffix: yyyy-ww (KST 기준 ZonedDateTime 입력) */
  public String weeklySuffix(ZonedDateTime timeKst) {
    return weeklySuffix(timeKst.toLocalDate());
  }

  // ---------------- Keys (Cluster-safe with hash tags) ----------------
  /** 개인 월간: user_ranking:{yyyy-MM} */
  public String userMonthlyKey(String monthlySuffix) {
    return USER_RANKING_KEY_PREFIX + "{" + monthlySuffix + "}";
  }

  /** 팀 간 월간(평균): inter_team_ranking:{yyyy-MM} */
  public String interTeamMonthlyKey(String monthlySuffix) {
    return INTER_TEAM_RANKING_KEY_PREFIX + "{" + monthlySuffix + "}";
  }

  /** 팀 간 월간(합계): team_score_sum:{yyyy-MM} */
  public String teamScoreSumMonthlyKey(String monthlySuffix) {
    return TEAM_SCORE_SUM_KEY_PREFIX + "{" + monthlySuffix + "}";
  }

  /** 월초 팀 멤버수 스냅샷: team_member_count:{yyyy-MM} */
  public String teamMemberCountMonthlyHash(String monthlySuffix) {
    return TEAM_MEMBER_COUNT_HASH_PREFIX + "{" + monthlySuffix + "}";
  }

  /** 주간 활성 팀 SET: active_intra_teams:{yyyy-ww} */
  public String activeIntraTeamWeeklyKey(String weeklySuffix) {
    return ACTIVE_INTRA_TEAM_KEY_PREFIX + "{" + weeklySuffix + "}";
  }

  /** 팀 내 주간 ZSET: team_ranking:{yyyy-ww}:{teamId} */
  public String intraTeamWeeklyKey(long teamId, String weeklySuffix) {
    return INTRA_TEAM_RANKING_KEY_PREFIX + "{" + weeklySuffix + "}:" + teamId;
  }

  /** 스냅샷 키: snapshot:<원본키>  (해시태그 유지되어 RENAME/COPY 안전) */
  public String snapshotKey(String originalKey) {
    return SNAPSHOT_PREFIX + originalKey;
  }

}