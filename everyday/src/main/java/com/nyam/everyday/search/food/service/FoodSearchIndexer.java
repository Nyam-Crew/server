package com.nyam.everyday.search.food.service;

import com.nyam.everyday.search.food.document.FoodDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * FoodSearchIndexer
 *
 * @author : 장소희
 * @fileName : FoodSearchIndexer
 * @since : 25. 8. 13.
 */

@Service
@RequiredArgsConstructor
public class FoodSearchIndexer {

    private final ElasticsearchOperations operations;

    private static final int BULK_THRESHOLD = 500;
    private final List<FoodDocument> buffer = new ArrayList<>();

    /** ETL 동안 계속 호출: 메모리 버퍼에 쌓음, 임계치 넘으면 즉시 flush */
    public synchronized void add(FoodDocument doc) {
        if (doc == null) return;
        buffer.add(doc);
        if (buffer.size() >= BULK_THRESHOLD) {
            flush();
        }
    }

    /** 루프 끝에서 한 번 호출: 남은 것 모두 저장 */
    public synchronized void flush() {
        if (buffer.isEmpty()) return;
        operations.save(buffer);        // @Document의 indexName("food") 기준으로 벌크 저장
        buffer.clear();
    }
}
