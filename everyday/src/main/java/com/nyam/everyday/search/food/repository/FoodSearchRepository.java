package com.nyam.everyday.search.food.repository;

import com.nyam.everyday.search.food.document.FoodDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * FoodSearchRepository
 *
 * @author : 장소희
 * @fileName : FoodSearchRepository
 * @since : 25. 8. 13.
 */

public interface FoodSearchRepository extends ElasticsearchRepository<FoodDocument, String>, FoodSearchRepositoryCustom {
    // 필요하면 메소드 쿼리 추가 가능
}