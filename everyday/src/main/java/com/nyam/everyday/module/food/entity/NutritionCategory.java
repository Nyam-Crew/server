package com.nyam.everyday.module.food.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

/**
 * NutritionCategory entity
 *
 * @author : 장소희
 * @fileName : NutritionCategory
 * @since : 25. 8. 5.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "nutrition_category")
public class NutritionCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("영양소 카테고리 ID")
    private Long foodCateId;

    @Comment("카테고리명")
    @Column(nullable = false, length = 10)
    private String category;
}