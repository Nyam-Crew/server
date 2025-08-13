package com.nyam.everyday.search.bootstrap;

import com.nyam.everyday.search.document.FoodDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

/**
 * FoodIndexBootstrap
 *
 * @author : 장소희
 * @fileName : FoodIndexBootstrap
 * @since : 25. 8. 13.
 *
 * 앱 시작 시 ES 인덱스 자동 생성/검증
 *  - FoodDocument에 선언한 @Setting/@Mapping을 읽어서 생성
 *  - 이미 있으면 건드리지 않음(매핑/분석기 변경은 재색인 전략 필요)
 *
 */

@Slf4j
@Component
@Profile({"local", "dev"}) // 운영은 보통 수동/배치로 관리. 원하면 제거해도 됨.
@RequiredArgsConstructor
public class FoodIndexBootstrap implements ApplicationRunner {

    private final ElasticsearchOperations operations;

    @Override
    public void run(ApplicationArguments args) {
        IndexOperations indexOps = operations.indexOps(FoodDocument.class);

        if (!indexOps.exists()) {

            boolean created = indexOps.create();
            if (created) {
                indexOps.putMapping(indexOps.createMapping());
                log.info("[ES] index 'food' created with settings/mappings.");
            } else {
                log.warn("[ES] index 'food' create() returned false.");
            }
        } else {
            log.info("[ES] index 'food' already exists. (no changes applied)");

        }
    }
}