package com.nyam.everyday.module.mission.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/*
 * 요약 데이터 조회 전용 Repository
 *
 * 설계 의도
 * - DB 접근만 담당 (JDBC Template 사용)
 * - 결과 없음(null)도 상위 레이어가 단순화되도록 기본값(0, false) 보장
 * - 비즈니스 규칙은 Service에서 처리
 */
@Repository
@RequiredArgsConstructor
public class SummaryQueryRepository {

    private static final String SQL_TOTAL_WATER = """
        SELECT COALESCE(total_water, 0)
        FROM member_daily_summary
        WHERE member_id = :mid AND summary_date = :d
        """;

    private static final String SQL_EXISTS_MEAL_LOG = """
        SELECT EXISTS (
          SELECT 1
          FROM member_meal_log
          WHERE member_id = :mid AND meal_log_date = :d
        )
        """;

    private static final String SQL_HAS_WEIGHT = """
        SELECT EXISTS (
          SELECT 1
          FROM member_daily_summary
          WHERE member_id = :mid AND summary_date = :d AND weight IS NOT NULL
        )
        """;

    private final NamedParameterJdbcTemplate jdbc;

    /* 해당 날짜의 총 물 섭취량(ml). 없으면 0.0 */
    public double getTotalWater(Long memberId, LocalDate date) {
        var params = new MapSqlParameterSource()
                .addValue("mid", memberId)
                .addValue("d", date);
        Double v = jdbc.queryForObject(SQL_TOTAL_WATER, params, Double.class);
        return v != null ? v : 0.0d;
    }

    /* 해당 날짜에 식단 로그 존재 여부 */
    public boolean existsMealLog(Long memberId, LocalDate date) {
        var params = new MapSqlParameterSource()
                .addValue("mid", memberId)
                .addValue("d", date);
        Boolean v = jdbc.queryForObject(SQL_EXISTS_MEAL_LOG, params, Boolean.class);
        return Boolean.TRUE.equals(v);
    }

    /* 해당 날짜에 체중 기록 존재 여부 */
    public boolean hasWeight(Long memberId, LocalDate date) {
        var params = new MapSqlParameterSource()
                .addValue("mid", memberId)
                .addValue("d", date);
        Boolean v = jdbc.queryForObject(SQL_HAS_WEIGHT, params, Boolean.class);
        return Boolean.TRUE.equals(v);
    }
}