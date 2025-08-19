package com.nyam.everyday.module.team.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * 실시간 피드용 식사 시간대
 *
 * @author : 이지은
 * @fileName : MealPeriod
 * @since : 25. 8. 18.
 *
 */
@Getter
@RequiredArgsConstructor
public enum MealPeriod {
    MORNING("아침"),
    LUNCH("점심"),
    DINNER("저녁"),
    SNACK("간식");

    private final String label;
}
