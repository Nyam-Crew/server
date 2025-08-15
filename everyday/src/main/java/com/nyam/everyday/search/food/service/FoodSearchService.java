package com.nyam.everyday.search.food.service;


import com.nyam.everyday.search.food.dto.*;
import com.nyam.everyday.search.food.mapper.FoodSearchMapper;
import com.nyam.everyday.search.food.repository.FoodSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * FoodSearchService
 *
 * @author : 장소희
 * @fileName : FoodSearchService
 * @since : 25. 8. 15.
 */

@Service
@RequiredArgsConstructor
public class FoodSearchService {

    private final FoodSearchRepository repository;
    private final FoodSearchMapper mapper;

    public FoodSearchResponse search(FoodSearchRequest req) {
        var pageable = PageRequest.of(req.page(), req.size());
        var page = repository.search(req.q(), req.manufacturer(), pageable, req.sort());
        return mapper.toPageResponse(page);
    }

    public FoodSuggestionResponse suggest(String prefix, Integer size) {
        var list = repository.suggestPrefix(prefix, size == null ? 10 : size);
        return new FoodSuggestionResponse(list);
    }
}