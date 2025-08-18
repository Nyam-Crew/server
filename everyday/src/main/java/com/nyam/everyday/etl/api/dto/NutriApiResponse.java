package com.nyam.everyday.etl.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
/**
 * NutriApiResponse
 *
 * @author : 장소희
 * @fileName : NutriApiResponse
 * @since : 25. 8. 12.
 */

/** OpenAPI 최상위 래퍼: { "response": { header, body } } */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NutriApiResponse {

    @JsonProperty("response")
    private Response response;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        @JsonProperty("header") private NutriApiHeader header;
        @JsonProperty("body")   private NutriApiBody body;

    }
}
