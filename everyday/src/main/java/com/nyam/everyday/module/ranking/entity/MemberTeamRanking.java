package com.nyam.everyday.module.ranking.entity;

import com.nyam.everyday.common.entity.BaseCreatedEntity;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.team.entity.Team;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

/** 팀 내부 랭킹 히스토리 (주간) */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "member_team_ranking")
public class MemberTeamRanking extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("기본키")
    private Long id;

    @Column(nullable = false)
    @Comment("랭킹 연도")
    private Integer rankingYear;

    @Column(nullable = false)
    @Comment("랭킹 주차")
    private Integer rankingWeek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    @Comment("팀 내 순위")
    private Integer rankInTeam;

    @Column(nullable = false)
    @Comment("점수")
    private Integer score;
}
