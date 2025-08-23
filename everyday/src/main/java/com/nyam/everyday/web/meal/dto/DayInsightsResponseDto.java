package com.nyam.everyday.web.meal.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DayInsightsResponseDto {
    private Long memberId;
    private Integer age;

    private BigDecimal bmi;   // null 허용
    private BigDecimal bmr;   // null 허용
    private BigDecimal tdee;  // null 허용
    private Integer recommendedCalories; // null 허용

    private BigDecimal totalProtein;       // 요약 값(없으면 0)
    private BigDecimal totalCarbohydrate;  // 요약 값(없으면 0)
    private BigDecimal totalFat;           // 요약 값(없으면 0)
    private BigDecimal totalWater;         // 요약 값(없으면 0)
    private BigDecimal totalKcal;          // 요약 값(없으면 0)
}