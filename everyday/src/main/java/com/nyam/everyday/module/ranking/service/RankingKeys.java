package com.nyam.everyday.module.ranking.service;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * 랭킹/통계 Redis 키 생성을 단일화합니다.
 * - 월간 suffix: yyyy-MM
 * - 주간 suffix: yyyy-ww (week-based-year 기준)
 */
@Component
public class RankingKeys {

  public static final String USER_RANKING_KEY_PREFIX        = "user_ranking:";          // ZSET(memberId -> totalScore)
  public static final String INTRA_TEAM_RANKING_KEY_PREFIX  = "team_ranking:";          // ZSET(memberId -> score) per team/week
  public static final String INTER_TEAM_RANKING_KEY_PREFIX  = "inter_team_ranking:";    // ZSET(teamId -> avgScore) per month
  public static final String TEAM_SCORE_SUM_KEY_PREFIX      = "team_score_sum:";        // ZSET(teamId -> totalScore) per month
  public static final String TEAM_MEMBER_COUNT_HASH_PREFIX  = "team_member_count:";     // HASH(teamId -> count) per month
  public static final String ACTIVE_INTRA_TEAM_KEY_PREFIX   = "active_intra_teams:";    // SET(teamId) per week
  public static final String SNAPSHOT_PREFIX                = "snapshot:";              // rename 스냅샷 접두사

  private static final DateTimeFormatter YM_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
  private final ZoneId zoneId = ZoneId.of("Asia/Seoul");

  public ZoneId zoneId() { return zoneId; }

  // ----- Suffix -----
  public String monthlySuffix(YearMonth ym) {
    return ym.format(YM_FMT);
  }

  public String monthlySuffix(ZonedDateTime timeKst) {
    return YearMonth.from(timeKst).format(YM_FMT);
  }

  /** week-based-year + week-of-week-based-year 기준 */
  public String weeklySuffix(LocalDate dateKst) {
    var wf = WeekFields.of(Locale.KOREA);
    int week = dateKst.get(wf.weekOfWeekBasedYear());
    int year = dateKst.get(wf.weekBasedYear());
    return String.format("%d-%02d", year, week);
  }

  public String weeklySuffix(ZonedDateTime timeKst) {
    return weeklySuffix(timeKst.toLocalDate());
  }

  // ----- Keys -----
  public String userMonthlyKey(String monthlySuffix)             { return USER_RANKING_KEY_PREFIX + monthlySuffix; }
  public String interTeamMonthlyKey(String monthlySuffix)        { return INTER_TEAM_RANKING_KEY_PREFIX + monthlySuffix; }
  public String teamScoreSumMonthlyKey(String monthlySuffix)     { return TEAM_SCORE_SUM_KEY_PREFIX + monthlySuffix; }
  public String teamMemberCountMonthlyHash(String monthlySuffix) { return TEAM_MEMBER_COUNT_HASH_PREFIX + monthlySuffix; }
  public String activeIntraTeamWeeklyKey(String weeklySuffix)    { return ACTIVE_INTRA_TEAM_KEY_PREFIX + weeklySuffix; }
  public String intraTeamWeeklyKey(long teamId, String weeklySuffix) {
    return INTRA_TEAM_RANKING_KEY_PREFIX + teamId + ":" + weeklySuffix;
  }

  public String snapshotKey(String originalKey) { return SNAPSHOT_PREFIX + originalKey; }

  public byte[] bytes(String s) { return s.getBytes(StandardCharsets.UTF_8); }
}
