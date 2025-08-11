package com.nyam.everyday.module.team.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 그룹 간 경쟁에 대한 랭킹 백업
 *
 * @author : 이지은
 * @fileName : TeamGlobalRanking
 * @since : 25. 8. 6.
 */
@Entity
@Table(name = "team_global_ranking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamGlobalRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "period_type", nullable = false, length = 10)
    private String periodType;

    @Column(name = "period_value", nullable = false, length = 10)
    private String periodValue;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "total_score")
    private Integer totalScore;

    @Column(name = "rank")
    private Integer rank;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;
}