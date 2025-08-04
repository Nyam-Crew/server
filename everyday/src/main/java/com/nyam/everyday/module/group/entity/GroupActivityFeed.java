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
 * 그룹 멤버 실시간 현황 관련 entity
 *
 * @author : 이지은
 * @fileName : GroupActivityFeed
 * @since : 25. 8. 4.
 */

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "group_activity_feed")
public class GroupActivityFeed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feed_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "activity_type", nullable = false)
    private String activityType;

    @Column(name = "activity_content")
    private String activityContent;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}