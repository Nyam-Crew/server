package com.nyam.everyday.module.team.entity;

import com.nyam.everyday.common.entity.BaseCreatedEntity;
import com.nyam.everyday.module.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
/**
 * 그룹 내 랭킹 캐시
 *
 * @author : 이지은
 * @fileName : teamRankingHistory
 * @since : 25. 8. 4.
 */

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "team_ranking_history")
public class TeamRankingHistory extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_ranking_id")
    private Long teamRankingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "week_code", nullable = false)
    private String weekCode;

    @Column(nullable = false)
    private int point;

    @Column(nullable = false)
    private String field;

}