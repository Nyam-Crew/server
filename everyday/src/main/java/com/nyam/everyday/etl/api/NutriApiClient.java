package com.nyam.everyday.etl.api;

import com.nyam.everyday.etl.api.dto.NutriApiItem;
import com.nyam.everyday.etl.api.dto.NutriApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * NutriApiClient
 *
 * @author : 장소희
 * @fileName : NutriApiClient
 * @since : 25. 8. 12.
 * 영양성분 OpenAPI 호출 클라이언트.
 * - Spring 6 RestClient 사용(간결, 테스트 용이)
 */



@Slf4j
@Component
@EnableConfigurationProperties(NutriApiProperties.class)
public class NutriApiClient {

    private final RestClient http;
    private final NutriApiProperties props;

    public NutriApiClient(NutriApiProperties props) {
        this.props = props;

        // 1) 재인코딩 방지
        DefaultUriBuilderFactory ubf = new DefaultUriBuilderFactory(props.getBaseUrl());
        ubf.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        // 2) 타임아웃 (간단 버전)
        var rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        rf.setReadTimeout((int) Duration.ofSeconds(10).toMillis());

        this.http = RestClient.builder()
                .uriBuilderFactory(ubf)
                .requestFactory(rf)
                .build();
    }

    /** 1페이지 조회 */
    public List<NutriApiItem> fetchItems(int page, int rows) {
        int safePage = Math.max(1, page);
        int safeRows = rows > 0 ? rows : props.getDefaultPageSize();

        MultiValueMap<String, String> q = new LinkedMultiValueMap<>();
        q.add("serviceKey", props.getServiceKey()); // 이미 인코딩된 키면 그대로!
        q.add("type", "json");
        q.add("pageNo", String.valueOf(safePage));
        q.add("numOfRows", String.valueOf(safeRows));

        try {
            // 호출 전 URL 미리 로깅
            String previewUrl = props.getBaseUrl() + "?pageNo=" + safePage + "&numOfRows=" + safeRows + "&type=json&serviceKey=****";
            log.info("[NutriApi] GET {}", previewUrl);

            NutriApiResponse res = http.get()
                    .uri(b -> b.queryParams(q).build())
                    .retrieve()
                    .body(NutriApiResponse.class);

            if (res == null || res.getResponse() == null ||
                    res.getResponse().getHeader() == null ||
                    res.getResponse().getBody() == null) {
                throw new IllegalStateException("응답 파싱 실패(page=" + safePage + ")");
            }

            var header = res.getResponse().getHeader();
            String code = header.getResultCode();
            String msg  = header.getResultMsg();
            log.debug("[NutriApi] resultCode={}, resultMsg={}", code, msg);

            if (!"00".equals(code)) {
                throw new IllegalStateException("OpenAPI 오류: " + code + " / " + msg + " (page=" + safePage + ")");
            }

            List<NutriApiItem> items = res.getResponse().getBody().getItems();
            return (items != null) ? items : Collections.emptyList();

        } catch (RestClientResponseException ex) {
            log.error("[NutriApi] HTTP {} : {}", ex.getRawStatusCode(), cut(ex.getResponseBodyAsString(), 500));
            throw new IllegalStateException("HTTP 오류 " + ex.getRawStatusCode(), ex);
        } catch (Exception ex) {
            log.error("[NutriApi] 호출 실패 page={}, rows={}", safePage, safeRows, ex);
            throw new IllegalStateException("API 호출 실패(page=" + safePage + ", rows=" + safeRows + ")", ex);
        }
    }

    private static String cut(String s, int max) {
        return (s == null || s.length() <= max) ? s : s.substring(0, max) + "...";
    }
}