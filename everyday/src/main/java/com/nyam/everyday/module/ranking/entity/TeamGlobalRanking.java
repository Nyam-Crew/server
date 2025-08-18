package com.nyam.everyday.module.ranking.entity;

import com.nyam.everyday.common.entity.BaseCreatedEntity;
import com.nyam.everyday.module.team.entity.Team;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "team_global_ranking")
public class TeamGlobalRanking extends BaseCreatedEntity {

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
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false)
    @Comment("순위")
    private Integer rank;

    @Column(nullable = false, precision = 10, scale = 2)
    @Comment("평균 점수")
    private BigDecimal averageScore;

    @Column(nullable = false)
    @Comment("총 점수")
    private Long totalScore;

    @Column(nullable = false)
    @Comment("팀원 수")
    private Integer memberCount;
}
