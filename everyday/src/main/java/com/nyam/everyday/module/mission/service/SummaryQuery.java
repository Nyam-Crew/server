package com.nyam.everyday.module.mission.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SummaryQuery {

    private final NamedParameterJdbcTemplate jdbc;

    public double getTotalWater(Long memberId, LocalDate date) {
        String sql = """
            select coalesce(total_water, 0) 
            from member_daily_summary 
            where member_id = :mid and summary_date = :d
        """;
        var p = new MapSqlParameterSource()
                .addValue("mid", memberId)
                .addValue("d", date);
        Double v = jdbc.query(sql, p, rs -> rs.next() ? rs.getDouble(1) : 0.0);
        return v != null ? v : 0.0;
    }

    public boolean existsMealLog(Long memberId, LocalDate date) {
        String sql = """
            select exists(
              select 1 from member_meal_log 
              where member_id = :mid and meal_log_date = :d
            )
        """;
        var p = new MapSqlParameterSource().addValue("mid", memberId).addValue("d", date);
        Boolean v = jdbc.query(sql, p, rs -> rs.next() && rs.getBoolean(1));
        return Boolean.TRUE.equals(v);
    }

    public boolean hasWeight(Long memberId, LocalDate date) {
        String sql = """
            select exists(
              select 1 from member_daily_summary
              where member_id = :mid and summary_date = :d and weight is not null
            )
        """;
        var p = new MapSqlParameterSource().addValue("mid", memberId).addValue("d", date);
        Boolean v = jdbc.query(sql, p, rs -> rs.next() && rs.getBoolean(1));
        return Boolean.TRUE.equals(v);
    }
}