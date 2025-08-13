package com.nyam.everyday.search.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * ElasticsearchConfig
 *
 * @author : 장소희
 * @fileName : ElasticsearchConfig
 * @since : 25. 8. 13.
 *
 * - spring.elasticsearch.* 설정은 application-*.yml 에서 자동 바인딩되고,
 *   여기서는 ES Repository 스캔만 활성화합니다.
 *
 */

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.nyam.everyday.search.repository")
public class ElasticsearchConfig {
}
