package com.nyam.everyday.etl.api;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;
/**
 * NutriApiProperties
 *
 * @author : 장소희
 * @fileName : NutriApiProperties
 * @since : 25. 8. 12.
 *
 * application.yml 의 openapi.nutri.* 바인딩
 */

@Setter
@Getter
@ConfigurationProperties(prefix = "openapi.nutri")
public class NutriApiProperties {
    private String baseUrl;       // 예: http://api.data.go.kr/openapi/tn_pubr_public_nutri_process_info_api
    private String serviceKey;    // URL-encoded key
    private int defaultPageSize = 100;
    /** 연결 타임아웃 (기본 10초) */
    private Duration connectTimeout = Duration.ofSeconds(10);
    /** 읽기 타임아웃 (기본 60초) */
    private Duration readTimeout = Duration.ofSeconds(60);
}