package com.nyam.everyday.module.food.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import java.math.BigDecimal;

/**
 * NutritionDetail entity
 *
 * @author : 장소희
 * @fileName : NutritionDetail
 * @since : 25. 8. 5.
 */


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "nutrition_detail")
public class NutritionDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("영양소 상세 ID")
    private Long nutritionId;

    @Comment("음식 ID")
    @Column(nullable = false)
    private Long foodId;

    @Comment("카테고리 ID")
    @Column(nullable = false)
    private Long foodCateId;

    @Comment("영양소명")
    @Column(nullable = false, length = 50)
    private String nutritionNm;

    @Comment("영양성분 양")
    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal amount;

    @Comment("단위 무게 (g)")
    @Column(nullable = false)
    private Long unitWeight;
}
