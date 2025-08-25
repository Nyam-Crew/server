package com.nyam.everyday.search.food.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
/**
 * FoodDocument
 *
 * @author : 장소희
 * @fileName : FoodDocument
 * @since : 25. 8. 13.
 *
 * Elasticsearch에 저장되는 Food 문서 모델
 * - indexName = "food"  (우리가 만든 인덱스와 동일)
 * - 검색: foodName(Text + nori), 정합/필터: manufacturer(Keyword)
 * - 표시: unitKcal, unitGram
 *
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "food")
@Setting(settingPath = "/elasticsearch/food/food-settings.json")
@Mapping(mappingPath = "/elasticsearch/food/food-mappings.json")
public class FoodDocument {

    @Id
    private String id;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "korean_search", searchAnalyzer = "korean_search"),
            otherFields = { @InnerField(suffix = "keyword", type = FieldType.Keyword) }
    )
    private String foodName;

    /** 검색은 manufacturer.search(text), 필터는 manufacturer(keyword) */
    @Field(type = FieldType.Keyword)
    private String manufacturer;

    @Field(type = FieldType.Double)
    private Double unitKcal;

    @Field(type = FieldType.Long)
    private Long unitGram;

    @Field(type = FieldType.Integer)
    private Integer foodSize;

    /** copy_to 대상으로만 쓰는 통합 검색 필드 (명시 보관용) */
    @Field(type = FieldType.Text, analyzer = "korean_search", searchAnalyzer = "korean_search")
    private String all_search;
}