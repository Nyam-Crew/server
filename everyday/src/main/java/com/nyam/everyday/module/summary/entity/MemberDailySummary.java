package com.nyam.everyday.module.summary.entity;

import com.nyam.everyday.module.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(
        name = "member_daily_summary",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_member_summary_day", columnNames = {"member_id", "summary_date"})
        }
)
public class MemberDailySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_daily_id")
    @Comment("회원 일일 요약 PK")
    private Long memberDailyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @Comment("회원 FK")
    private Member member;

    @Comment("요약 날짜")
    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    @Comment("체중(kg)")
    @Column(name = "weight", precision = 4, scale = 1, nullable = false)
    private BigDecimal weight;

    @Comment("총 단백질(g)")
    @Column(name = "total_protein", precision = 5, scale = 1, nullable = false)
    private BigDecimal totalProtein;

    @Comment("총 탄수화물(g)")
    @Column(name = "total_carbohydrate", precision = 5, scale = 1, nullable = false)
    private BigDecimal totalCarbohydrate;

    @Comment("총 지방(g)")
    @Column(name = "total_fat", precision = 5, scale = 1, nullable = false)
    private BigDecimal totalFat;

    @Comment("총 물 섭취량(ml)")
    @Column(name = "total_water", precision = 5, scale = 1, nullable = false)
    private BigDecimal totalWater;

    @Comment("총 칼로리(kcal)")
    @Column(name = "total_kcal", nullable = false)
    private Integer totalKcal;

    @Comment("생성일시")
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Comment("수정일시")
    @Column(name = "modified_date", nullable = false)
    private LocalDateTime modifiedDate;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdDate == null) createdDate = now;
        modifiedDate = now;
    }

    @PreUpdate
    void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}