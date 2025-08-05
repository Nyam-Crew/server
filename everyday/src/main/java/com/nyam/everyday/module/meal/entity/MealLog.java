package com.nyam.everyday.module.meal.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

import org.hibernate.annotations.Comment;

/**
 * MealLog
 *
 * @author : 장소희
 * @fileName : MealLog
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

    @Comment("회원 PK")
    @Column(nullable = false)
    private Long memberId;

    @Comment("음식 PK")
    @Column(nullable = false)
    private Long foodId;

    @Comment("섭취량")
    @Column(nullable = false)
    private Integer intakeAmount;

    @Comment("섭취 칼로리")
    @Column(nullable = false, precision = 6, scale = 1)
    private BigDecimal intakeKcal;

    @Comment("식사 타입(0:아침, 1:점심, 2:저녁, 3:간식)")
    @Column(nullable = false, length = 2)
    private String mealType;

    @Comment("생성일시")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Comment("수정일시")
    @Column(nullable = false)
    private LocalDateTime modifiedDate;
}