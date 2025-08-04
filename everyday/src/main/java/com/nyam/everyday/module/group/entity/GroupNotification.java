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
 * 그룹 내 알림 Entity
 *
 * @author : 이지은
 * @fileName : Group_Notification
 * @since : 25. 8. 4.
 */

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "group_notification")
public class GroupNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_alarm_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "notification_type", nullable = false)
    private String notificationType;

    @CreatedDate
    @Column(name = "created_date")
    private LocalDateTime createdDate;
}