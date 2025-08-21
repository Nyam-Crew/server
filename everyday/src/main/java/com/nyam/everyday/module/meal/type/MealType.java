package com.nyam.everyday.module.meal.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 식사 유형 (아침, 점심, 저녁, 간식)
 * @author : 장소희, 이지은 (통합)
 * @fileName : MealType
 * @since : 25. 8. 20.
 */
@Getter
@RequiredArgsConstructor
public enum MealType {
    BREAKFAST("아침"),
    LUNCH("점심"),
    DINNER("저녁"),
    SNACK("간식");

    private final String label;
}