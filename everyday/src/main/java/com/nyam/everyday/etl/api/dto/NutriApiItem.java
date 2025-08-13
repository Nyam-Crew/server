package com.nyam.everyday.etl.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * NutriApiItem
 *
 * @author : 장소희
 * @fileName : NutriApiItem
 * @since : 25. 8. 12.
 * OpenAPI "items" 한 건 모델.
 *  - 숫자도 문자열로 받고, 변환은 서비스/유틸에서 일괄 처리(깨끗한 책임 분리).
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NutriApiItem {

    @JsonProperty("foodCd") private String foodCd;
    @JsonProperty("foodNm") private String foodNm;
    @JsonProperty("mfrNm")  private String mfrNm;

    @JsonProperty("nutConSrtrQua") private String nutConSrtrQua; // "100g", "100ml"
    @JsonProperty("foodSize")      private String foodSize;      // "1000g", "85ml"

    // 에너지/주요영양소
    @JsonProperty("enerc") private String enerc;  // kcal
    @JsonProperty("prot")  private String prot;   // g
    @JsonProperty("fatce") private String fatce;  // g
    @JsonProperty("chocdf")private String chocdf; // g
    @JsonProperty("sugar") private String sugar;  // g
    @JsonProperty("fibtg") private String fibtg;  // g

    // 미네랄/지방산 등
    @JsonProperty("nat")   private String nat;    // mg
    @JsonProperty("chole") private String chole;  // mg
    @JsonProperty("fasat") private String fasat;  // g
    @JsonProperty("fatrn") private String fatrn;  // g

}
