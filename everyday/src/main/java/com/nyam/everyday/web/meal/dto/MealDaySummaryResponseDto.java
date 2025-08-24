package com.nyam.everyday.web.meal.dto;

import com.nyam.everyday.module.meal.type.MealType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/*
 * 하루 요약 응답 DTO
 *
 * 설계 의도
 * - 하루 단위로 섭취 기록, 물, 체중 데이터를 요약 반환
 * - Map<MealType, MealSummary> 구조로 아침/점심/저녁/간식별 합계 표시
 * - water/weight는 null 가능 (기록 없을 경우)
 */
@Getter
@Builder
@Schema(description = "하루 요약 응답 DTO")
public class MealDaySummaryResponseDto {

    @Schema(description = "요약 대상 날짜")
    private Date date;

    @Schema(description = "식사 타입별 요약 (아침/점심/저녁/간식)")
    private Map<MealType, MealSummary> meals;

    @Schema(description = "하루 물 섭취량 (기록 없으면 null)")
    private BigDecimal water;

    @Schema(description = "하루 체중 (기록 없으면 null)")
    private BigDecimal weight;

    @Getter
    @Builder
    @Schema(description = "식사별 요약 정보")
    public static class MealSummary {

        @Schema(description = "식사별 총 칼로리 합계")
        private BigDecimal totalKcal;

        @Schema(description = "식사 여부 (true=먹음, false=안먹음, null=기록 없음)")
        private Boolean takeMeal;
    }
}