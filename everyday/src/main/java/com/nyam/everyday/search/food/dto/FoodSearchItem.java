package com.nyam.everyday.search.food.dto;

import lombok.Builder;

/**
 * FoodSearchItem
 *
 * @author : 장소희
 * @fileName : FoodSearchItem
 * @since : 25. 8. 15.
 */

@Builder
public record FoodSearchItem(
        String id,
        String foodName,
        String manufacturer,
        Double unitKcal,
        Long unitGram,
        Integer foodSize
) {}