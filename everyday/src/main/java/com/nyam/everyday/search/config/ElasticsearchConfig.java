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
 * 이미 Spring Boot auto-config + @Document를 쓰고 있으면 없어도 동작하지만, 리포지토리 스캔 경로를 명확히 해두면 안전함
 *
 */

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.nyam.everyday.search")
public class ElasticsearchConfig {
}
