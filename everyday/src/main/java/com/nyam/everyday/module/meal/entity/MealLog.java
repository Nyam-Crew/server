package com.nyam.everyday.module.meal.entity;

import com.nyam.everyday.module.food.entity.Food;
import com.nyam.everyday.module.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.Comment;

/**
 * MealLog
 *
 * @author : 장소희
 * @fileName : MealLog entity
 * @since : 25. 8. 5.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "member_meal_log")
public class MealLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("음식 기록 PK")
    private Long mealLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Comment("섭취량")
    @Column(nullable = false)
    private Integer intakeAmount;

    @Comment("섭취 칼로리")
    @Column(nullable = false, precision = 6, scale = 1)
    private BigDecimal intakeKcal;

    @Schema(description = "식사 타입 (BREAKFAST:아침, LUNCH:점심, DINNER:저녁, SNACK:간식)")
    @Column(nullable = false, length = 16)
    private String mealType;

    @Comment("단백질")
    @Column(precision = 4, scale = 1, nullable = false)
    private BigDecimal protein;

    @Comment("지방")
    @Column(precision = 4, scale = 1, nullable = false)
    private BigDecimal fat;

    @Comment("탄수화물")
    @Column(precision = 4, scale = 1, nullable = false)
    private BigDecimal carbohydrate;

    @Comment("생성일시")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Comment("수정일시")
    @Column(nullable = false)
    private LocalDateTime modifiedDate;
}