package com.nyam.everyday.search.document;

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
public class FoodDocument {

    @Id
    private String id;               // ES 문서 ID (RDB PK를 문자열로 써도 되고, ES가 생성해도 됨)

    /** 음식 이름: 한국어 형태소 분석 + 정확일치 서브필드(keyword) */
    @MultiField(
            mainField = @Field(type = FieldType.Text,
                    analyzer = "korean_search",
                    searchAnalyzer = "korean_search"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String foodName;

    /** 제조사: 필터/정확일치 중심이면 Keyword */
    @Field(type = FieldType.Keyword)
    private String manufacturer;

    /** 100g 기준 열량 (표시/정렬 등에 사용) */
    // 매핑을 scaled_float로 만들었더라도 number 전송은 가능하므로 Double로 둬도 OK
    @Field(type = FieldType.Double)
    private Double unitKcal;

    /** 기준 g (항상 100 고정으로 넣을 예정) */
    @Field(type = FieldType.Long)
    private Long unitGram;

    @Field(type = FieldType.Integer)
    private Integer foodSize;
}