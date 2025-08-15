package com.nyam.everyday.search.food.dto;

import lombok.Builder;

import java.util.List;

/**
 * FoodSearchResponse
 *
 * @author : 장소희
 * @fileName : FoodSearchResponse
 * @since : 25. 8. 15.
 */

@Builder
public record FoodSearchResponse(
        List<FoodSearchItem> content,
        long totalElements,
        int totalPages,
        int page,
        int size
) {}