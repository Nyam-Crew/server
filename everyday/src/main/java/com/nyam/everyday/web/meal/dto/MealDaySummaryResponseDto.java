package com.nyam.everyday.web.meal.dto;

import com.nyam.everyday.module.meal.type.MealType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @author : 장소희
 * @fileName : MealDaySummaryResponseDto
 * @since : 25. 8. 22.
 */
@Getter
@Builder
public class MealDaySummaryResponseDto {
    private Date date;
    private Map<MealType, MealSummary> meals;
    private BigDecimal water;
    private BigDecimal weight;

    @Getter @Builder
    public static class MealSummary {
        private BigDecimal totalKcal;  // 합계 kcal
        private Boolean takeMeal;      // true=먹음, false=안먹음, null=아직 기록없음
    }
}
