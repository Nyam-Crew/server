package com.nyam.everyday.module.group.entity;

import com.nyam.everyday.module.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
/**
 * 그룹 내 랭킹 캐시
 *
 * @author : 이지은
 * @fileName : GroupRankingHistory
 * @since : 25. 8. 4.
 */

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "group_ranking_history")
public class GroupRankingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_ranking_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "week_code", nullable = false)
    private String weekCode;

    @Column(nullable = false)
    private int point;

    @Column(nullable = false)
    private String field;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}