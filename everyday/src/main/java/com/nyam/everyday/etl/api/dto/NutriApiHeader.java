package com.nyam.everyday.etl.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * NutriApiHeader
 *
 * @author : 장소희
 * @fileName : NutriApiHeader
 * @since : 25. 8. 12.
 */

/** OpenAPI response.header */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NutriApiHeader {
    @JsonProperty("resultCode") private String resultCode;
    @JsonProperty("resultMsg")  private String resultMsg;
    @JsonProperty("type")       private String type;
}
