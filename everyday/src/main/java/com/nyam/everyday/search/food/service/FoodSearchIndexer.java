package com.nyam.everyday.search.food.service;

import com.nyam.everyday.search.food.document.FoodDocument;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class FoodSearchIndexer {

    private final ElasticsearchOperations operations;

    private static final int BULK_THRESHOLD = 1500;

    private final List<FoodDocument> buffer = new ArrayList<>();

    @PostConstruct
    void initIndex() {
        IndexOperations io = operations.indexOps(FoodDocument.class);
        if (!io.exists()) {
            log.info("[ES] 'food' 인덱스가 없어 생성합니다.");
            // @Setting/@Mapping을 FoodDocument에 달아뒀다면 create() 가 그 설정을 사용
            io.create();
            io.putMapping(io.createMapping(FoodDocument.class));
            log.info("[ES] 'food' 인덱스 생성 완료.");
        } else {
            log.info("[ES] 'food' 인덱스가 이미 존재합니다.");
        }
    }

    /** ETL 중 계속 호출: 메모리 버퍼에 쌓고 임계치 넘으면 즉시 flush */
    public synchronized void add(FoodDocument doc) {
        if (doc == null) return;
        buffer.add(doc);
        if (buffer.size() >= BULK_THRESHOLD) {
            flush();
        }
    }

    /** 남은 것 모두 저장 + refresh(테스트/즉시검색용) */
    public synchronized void flush() {
        if (buffer.isEmpty()) return;
        try {
            log.info("[ES] bulk indexing {} docs...", buffer.size());
            operations.save(buffer);                  // @Document(indexName="food") 기반 벌크 저장
            operations.indexOps(FoodDocument.class).refresh(); // 바로 검색 가능하게
            log.info("[ES] bulk indexing done.");
        } catch (Exception e) {
            log.error("[ES] bulk indexing 실패", e);
        } finally {
            buffer.clear();
        }
    }

    @PreDestroy
    void onShutdown() {
        // 앱 종료 시 잔여 버퍼 밀어넣기
        flush();
    }
}