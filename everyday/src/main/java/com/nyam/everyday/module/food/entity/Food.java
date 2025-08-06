package com.nyam.everyday.module.food.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

/**
 * Food entity
 *
 * @author : 장소희
 * @fileName : Food
 * @since : 25. 8. 5.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "food")
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("음식 ID")
    private Long foodId;

    @Comment("음식명")
    @Column(nullable = false, length = 50)
    private String foodName;

    @Comment("제조사")
    @Column(length = 50)
    private String manufacturer;

    @Comment("단위 칼로리")

    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal unitKcal;

    @Comment("단위 무게 (g)")
    @Column(nullable = false)
    private Long unitWeight;
}
