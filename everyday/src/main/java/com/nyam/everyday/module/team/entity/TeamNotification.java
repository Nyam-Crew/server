package com.nyam.everyday.module.team.entity;

import com.nyam.everyday.common.entity.BaseCreatedEntity;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.team.enums.DeliveryStatus;
import com.nyam.everyday.module.team.enums.TeamNotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * 그룹 내 알림 Entity
 *
 * @author : 이지은
 * @fileName : Team_Notification
 * @since : 25. 8. 4.
 */

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "team_notification")
public class TeamNotification extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_alarm_id")
    private Long teamAlarmId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private TeamNotificationType notificationType;

    @Column(name = "team_noty_content", columnDefinition = "TEXT")
    private String teamNotyContent;

//    @Column(name = "noty_link")
//    private String notyLink;

    @Column(name = "is_checked")
    private Boolean isChecked = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("알림 전송 상태 (즉시/요약/대기)")
    private DeliveryStatus deliveryStatus;

    // --- 도메인 메서드 ---
    public void markAsBatched() {
        this.deliveryStatus = DeliveryStatus.BATCHED;
    }

    public void markAsImmediate() {
        this.deliveryStatus = DeliveryStatus.IMMEDIATE;
    }

    public void markAsPending() {
        this.deliveryStatus = DeliveryStatus.PENDING;
    }

    public void markAsProcessed() {
        this.deliveryStatus = DeliveryStatus.PROCESSED;
    }
}