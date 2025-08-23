package com.nyam.everyday.web.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/*
 * 월 단위 캘린더 응답 DTO
 *
 * 설계 의도
 * - 특정 연/월에 대한 캘린더 데이터를 반환
 * - days: 해당 월의 총 일수 (YearMonth.lengthOfMonth() 값)
 * - items: 각 날짜별 상세 데이터 리스트
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "월 단위 캘린더 응답 DTO")
public class MonthlyCalendarResponseDto {

    @Schema(description = "연도", example = "2025")
    private int year;

    @Schema(description = "월", example = "8")
    private int month;

    @Schema(description = "해당 월의 총 일수", example = "31")
    private int days;

    @Schema(description = "일 단위 데이터 리스트")
    private List<CalendarDayDto> items;
}