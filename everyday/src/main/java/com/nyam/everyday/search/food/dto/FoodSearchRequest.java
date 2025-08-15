package com.nyam.everyday.search.food.dto;

import jakarta.validation.constraints.Min;
import lombok.Builder;

/**
 * FoodSearchRequest
 *
 * @author : 장소희
 * @fileName : FoodSearchRequest
 * @since : 25. 8. 15.
 */


@Builder
public record FoodSearchRequest(
        String q,                 // 검색어 (식품명/제조사 동시)
        String manufacturer,      // 제조사 필터(정확일치, 선택)
        @Min(0) int page,
        @Min(1) int size,
        String sort               // 예: "score,desc" or "unitKcal,asc"
) {
    public static FoodSearchRequest of(String q, String manufacturer, Integer page, Integer size, String sort) {
        return FoodSearchRequest.builder()
                .q(q)
                .manufacturer(manufacturer)
                .page(page == null ? 0 : page)
                .size(size == null ? 10 : size)
                .sort(sort == null ? "score,desc" : sort)
                .build();
    }
}