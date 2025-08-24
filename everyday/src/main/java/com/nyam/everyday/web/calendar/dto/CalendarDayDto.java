package com.nyam.everyday.web.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/*
 * 캘린더 일 단위 데이터 DTO
 *
 * 설계 의도
 * - 캘린더 뷰에서 하루 단위 데이터(칼로리/체중/물 섭취/달성 여부)를 표시하기 위해 사용
 * - kcal은 기본 0, weight와 water는 기록이 없으면 null 처리
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "캘린더 일 단위 데이터 DTO")
public class CalendarDayDto {

    @Schema(description = "날짜 (yyyy-MM-dd)", example = "2025-08-22")
    private LocalDate date;

    @Schema(description = "섭취 칼로리 (없으면 0)", example = "1800")
    private BigDecimal kcal;

    @Schema(description = "체중 (kg, 없으면 null)", example = "64.2")
    private BigDecimal weight;

    @Schema(description = "물 섭취량 (ml, 없으면 null)", example = "1200")
    private Integer water;

    @Schema(description = "스탬프 달성 여부", example = "true")
    private boolean achieved;
}