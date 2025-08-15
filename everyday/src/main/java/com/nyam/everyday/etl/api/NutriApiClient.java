package com.nyam.everyday.etl.api;

import com.nyam.everyday.etl.api.dto.NutriApiBody;
import com.nyam.everyday.etl.api.dto.NutriApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.function.Supplier;

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

    // 재시도 기본값
    private static final int MAX_RETRY = 3;       // 3회
    private static final long BACKOFF_MS = 1000L; // 1s -> 2s -> 4s

    public NutriApiClient(NutriApiProperties props) {
        this.props = props;

        // 1) 재인코딩 방지
        DefaultUriBuilderFactory ubf = new DefaultUriBuilderFactory(props.getBaseUrl());
        ubf.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        // 2) 타임아웃 (프로퍼티 기반)
        var rf = new SimpleClientHttpRequestFactory();
        int connectMs = (int) props.getConnectTimeout().toMillis();
        int readMs    = (int) props.getReadTimeout().toMillis();
        rf.setConnectTimeout(connectMs);
        rf.setReadTimeout(readMs);

        this.http = RestClient.builder()
                .uriBuilderFactory(ubf)
                .requestFactory(rf)
                .build();
    }

    /** 1페이지 조회 (재시도 포함) */
    public NutriApiBody fetchItems(int page, int rows) {
        int safePage = Math.max(1, page);
        int safeRows = rows > 0 ? rows : props.getDefaultPageSize();

        MultiValueMap<String, String> q = new LinkedMultiValueMap<>();
        q.add("serviceKey", props.getServiceKey()); // 이미 인코딩된 키면 그대로
        q.add("type", "json");
        q.add("pageNo", String.valueOf(safePage));
        q.add("numOfRows", String.valueOf(safeRows));

        String previewUrl = props.getBaseUrl()
                + "?pageNo=" + safePage
                + "&numOfRows=" + safeRows
                + "&type=json&serviceKey=****";

        // 재시도 래퍼
        return withRetry(() -> {
            long t0 = System.currentTimeMillis();
            log.info("[NutriApi] GET {}", previewUrl);

            NutriApiResponse res = http.get()
                    .uri(b -> b.queryParams(q).build())
                    .retrieve()
                    .body(NutriApiResponse.class);

            if (res == null || res.getResponse() == null
                    || res.getResponse().getHeader() == null
                    || res.getResponse().getBody() == null) {
                throw new IllegalStateException("응답 파싱 실패(page=" + safePage + ")");
            }

            var header = res.getResponse().getHeader();
            String code = header.getResultCode();
            String msg  = header.getResultMsg();
            if (!"00".equals(code)) {
                throw new IllegalStateException("OpenAPI 오류: " + code + " / " + msg
                        + " (page=" + safePage + ")");
            }

            var body = res.getResponse().getBody();
            if (body.getItems() == null) {
                body.setItems(Collections.emptyList());
            }

            log.info("[NutriApi] OK page={}, rows={} -> items={}, {}ms",
                    safePage, safeRows, body.getItems().size(), System.currentTimeMillis() - t0);

            return body;
        }, "page=" + safePage + ", rows=" + safeRows);
    }

    /** 타임아웃/5xx에서 재시도(지수 백오프) */
    private <T> T withRetry(Supplier<T> call, String tag) {
        long wait = BACKOFF_MS;
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                return call.get();
            } catch (RestClientResponseException ex) {
                // 5xx만 재시도, 4xx는 즉시 실패
                int status = ex.getRawStatusCode();
                if (status >= 500 && attempt < MAX_RETRY) {
                    log.warn("[NutriApi][retry {}/{}] {} HTTP {}", attempt, MAX_RETRY, tag, status);
                } else {
                    log.error("[NutriApi] {} HTTP {} : {}", tag, status, cut(ex.getResponseBodyAsString(), 500));
                    throw new IllegalStateException("HTTP 오류 " + status, ex);
                }
            } catch (ResourceAccessException ex) {
                // 타임아웃/연결 문제 재시도
                if (attempt < MAX_RETRY) {
                    log.warn("[NutriApi][retry {}/{}] {} : {}", attempt, MAX_RETRY, tag, ex.getMessage());
                } else {
                    log.error("[NutriApi] {} : {}", tag, ex.getMessage());
                    throw new IllegalStateException("API 호출 실패(" + tag + ")", ex);
                }
            }

            // 백오프 후 재시도
            try { Thread.sleep(wait); } catch (InterruptedException ignored) {}
            wait *= 2; // 1s -> 2s -> 4s
        }
        throw new IllegalStateException("unreachable");
    }

    private static String cut(String s, int max) {
        return (s == null || s.length() <= max) ? s : s.substring(0, max) + "...";
    }
}