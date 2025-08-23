package com.nyam.everyday.module.mission.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 요약(요일별 합계/존재여부) 조회용 쿼리 모음.
 * - 읽기 전용 트랜잭션
 * - 기본값(0, false) 보장
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SummaryQuery {

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

    /** 해당 날짜의 총 물 섭취량(ml). 없으면 0.0 */
    public double getTotalWater(Long memberId, LocalDate date) {
        var params = new MapSqlParameterSource()
                .addValue("mid", memberId)
                .addValue("d", date);
        Double v = jdbc.queryForObject(SQL_TOTAL_WATER, params, Double.class);
        return v != null ? v : 0.0d;
    }

    /** 해당 날짜에 식단 로그가 존재하는지 */
    public boolean existsMealLog(Long memberId, LocalDate date) {
        var params = new MapSqlParameterSource()
                .addValue("mid", memberId)
                .addValue("d", date);
        Boolean v = jdbc.queryForObject(SQL_EXISTS_MEAL_LOG, params, Boolean.class);
        return Boolean.TRUE.equals(v);
    }

    /** 해당 날짜에 체중 기록이 존재하는지 */
    public boolean hasWeight(Long memberId, LocalDate date) {
        var params = new MapSqlParameterSource()
                .addValue("mid", memberId)
                .addValue("d", date);
        Boolean v = jdbc.queryForObject(SQL_HAS_WEIGHT, params, Boolean.class);
        return Boolean.TRUE.equals(v);
    }
}