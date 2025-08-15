/**
 * FoodSearchRepositoryImpl
 *
 * @author : 장소희
 * @fileName : FoodSearchRepositoryImpl
 * @since : 25. 8. 15.
 */

package com.nyam.everyday.search.food.repository;

import com.nyam.everyday.search.food.document.FoodDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FoodSearchRepositoryImpl implements FoodSearchRepositoryCustom {

    private final ElasticsearchOperations operations;

    @Override
    public Page<FoodDocument> search(String q, String manufacturer, Pageable pageable, String sort) {
        // 정렬 (_score 기본)
        Sort esSort = Sort.by(Sort.Order.desc("_score"));
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String field = parts[0].trim();
            Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1]))
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            esSort = "score".equalsIgnoreCase(field)
                    ? Sort.by(Sort.Order.by("_score").with(dir))
                    : Sort.by(new Sort.Order(dir, field));
        }

        Criteria criteria = new Criteria();
        boolean hasShould = false;

        if (q != null && !q.isBlank()) {
            // 1) 형태소 기반 match (foodName)
            Criteria c1 = new Criteria("foodName").matches(q);

            // 2) 제조사 텍스트 매치(형태소/standard로 저장되어 있으면 matches, keyword면 contains/startsWith에 맞춤)
            Criteria c2 = new Criteria("manufacturer").matches(q);

            // 3) 접두 매치: 전체 문자열 기준(prefix) -> keyword 서브필드 사용
            Criteria c3 = new Criteria("foodName.keyword").startsWith(q);

            // should 조합 (minimum_should_match = 1 효과)
            criteria = c1.or(c2).or(c3);
            hasShould = true;
        }

        if (manufacturer != null && !manufacturer.isBlank()) {
            // 정확 필터 필요 시 여기에 and 조건 (지금은 manufacturer 필터 안 쓸 거면 생략 가능)
            criteria = (hasShould ? criteria : new Criteria()).and(
                    new Criteria("manufacturer").is(manufacturer)
            );
        }

        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setPageable(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), esSort));

        SearchHits<FoodDocument> hits = operations.search(query, FoodDocument.class, IndexCoordinates.of("food"));
        List<FoodDocument> content = hits.stream().map(SearchHit::getContent).toList();
        long total = hits.getTotalHits();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<String> suggestPrefix(String prefix, int size) {
        if (prefix == null || prefix.isBlank()) return List.of();

        // 자동완성: 접두 우선 — keyword 서브필드로 빠르게
        CriteriaQuery query = new CriteriaQuery(
                new Criteria("foodName.keyword").startsWith(prefix)
        );
        query.setPageable(PageRequest.of(0, size));
        SearchHits<FoodDocument> hits = operations.search(query, FoodDocument.class, IndexCoordinates.of("food"));

        return hits.stream()
                .map(h -> h.getContent().getFoodName())
                .distinct()
                .limit(size)
                .toList();
    }
}