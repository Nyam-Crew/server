package com.nyam.everyday.etl.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*
 * NutriApiBody
 *
 * @author : 장소희
 * @fileName : NutriApiBody
 * @since : 25. 8. 12.
 */

/** OpenAPI response.body */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NutriApiBody {
    @JsonProperty("items")      private List<NutriApiItem> items;
    @JsonProperty("totalCount") private String totalCount;
    @JsonProperty("numOfRows")  private String numOfRows;
    @JsonProperty("pageNo")     private String pageNo;

}
