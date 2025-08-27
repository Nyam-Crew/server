package com.nyam.everyday.module.team.entity;

import com.nyam.everyday.common.entity.BaseCreatedEntity;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.team.enums.ActivityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;
/**
 * 그룹 멤버 실시간 현황 관련 entity -> 추후 db 저장할 일 생기면 활성화 및 DB에 테이블 생성해야함
 *
 * @author : 이지은
 * @fileName : TeamActivityFeed
 * @since : 25. 8. 4.
 */

//@Entity
//@Getter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@Table(name = "team_activity_feed")
// class TeamActivityFeed extends BaseCreatedEntity {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "feed_id")
//    private Long feedId;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id", nullable = false)
//    private Member member;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "team_id", nullable = false)
//    @OnDelete(action = OnDeleteAction.CASCADE)
//    private Team team;
//
//    @Column(name = "activity_type", nullable = false)
//    @Enumerated(EnumType.STRING)
//    private ActivityType activityType;
//
//    @Column(name = "activity_content")
//    private String activityContent;

//}