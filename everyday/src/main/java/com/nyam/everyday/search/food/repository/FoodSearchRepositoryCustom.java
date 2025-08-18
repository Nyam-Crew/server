package com.nyam.everyday.search.food.repository;

import com.nyam.everyday.search.food.document.FoodDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * FoodSearchRepositoryCustom
 *
 * @author : 장소희
 * @fileName : FoodSearchRepositoryCustom
 * @since : 25. 8. 15.
 */

public interface FoodSearchRepositoryCustom {
    Page<FoodDocument> search(String q, String manufacturer, Pageable pageable, String sort);
    List<String> suggestPrefix(String prefix, int size);
}