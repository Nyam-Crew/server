package com.nyam.everyday.module.ranking.entity;

import com.nyam.everyday.common.entity.BaseCreatedEntity;
import com.nyam.everyday.module.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

/** 전체 멤버 랭킹 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "member_global_ranking")
public class MemberGlobalRanking extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("기본키")
    private Long id;

    @Column(nullable = false)
    @Comment("랭킹 연도")
    private Integer rankingYear;

    @Column(nullable = false)
    @Comment("랭킹 월")
    private Integer rankingMonth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    @Comment("점수")
    private Integer score;

    @Column(nullable = false)
    @Comment("순위")
    private Integer rank;
}
