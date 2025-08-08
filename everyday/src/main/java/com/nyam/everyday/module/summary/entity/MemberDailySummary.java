package com.nyam.everyday.module.summary.entity;
import com.nyam.everyday.module.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MemberDailySummary
 *
 * @author : 장소희
 * @fileName : MemberDailySummary
 * @since : 25. 8. 7.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "member_daily_summary")
public class MemberDailySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("회원 일일 요약 PK")
    private Long memberDailyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Comment("체중")
    @Column(precision = 4, scale = 1)
    private BigDecimal weight;

    @Comment("총 단백질")
    @Column
    private Integer totalProtein;

    @Comment("총 탄수화물")
    @Column
    private Integer totalCarbohydrate;

    @Comment("총 지방")
    @Column
    private Integer totalFat;

    @Comment("총 물 섭취량")
    @Column
    private Integer totalWater;

    @Comment("총 칼로리")
    @Column(precision = 6, scale = 1)
    private BigDecimal totalKcal;

    @Comment("생성일시")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Comment("수정일시")
    @Column(nullable = false)
    private LocalDateTime modifiedDate;

}