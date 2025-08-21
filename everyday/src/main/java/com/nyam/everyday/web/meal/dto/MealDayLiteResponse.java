package com.nyam.everyday.web.meal.dto;

import com.nyam.everyday.module.meal.type.MealType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class MealDayLiteResponse {
    private LocalDate date;
    private Map<MealType, MealBucket> meals; // BREAKFAST/LUNCH/DINNER/SNACK
    private Integer water;                   // ml
    private BigDecimal weight;              // kg (소수1)
    private BigDecimal summaryTotalKcal;    // 일일 총칼로리
    private Meta meta;

    @Getter @Setter @Builder
    @AllArgsConstructor @NoArgsConstructor
    public static class MealBucket {
        private BigDecimal totalKcal;
        private List<MealItemLite> items;
    }

    @Getter @Setter @Builder
    @AllArgsConstructor @NoArgsConstructor
    public static class MealItemLite {
        private Long id;            // mealLogId
        private String foodName;
        private BigDecimal intakeKcal;
    }

    @Getter @Setter @Builder
    @AllArgsConstructor @NoArgsConstructor
    public static class Meta {
        private Instant updatedAt;
        private String etag;
    }
}