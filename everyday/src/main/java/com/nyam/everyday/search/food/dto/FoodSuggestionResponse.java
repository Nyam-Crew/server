package com.nyam.everyday.search.food.dto;

import java.util.List;

/**
 * FoodSuggestionResponse
 *
 * @author : 장소희
 * @fileName : FoodSuggestionResponse
 * @since : 25. 8. 15.
 */
public record FoodSuggestionResponse(List<String> suggestions) {}