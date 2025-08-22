package com.nyam.everyday.module.notification.entity;

import com.nyam.everyday.common.entity.BaseCreatedEntity;
import com.nyam.everyday.module.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseCreatedEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long notificationId;

  @JoinColumn(name = "member_id")
  @ManyToOne(fetch = FetchType.LAZY)
  @Comment("대상 멤버의 ID 지정, 없으면 전체 알림")
  private Member member;

  @Column(nullable = false, length = 255)
  @Comment("알림의 내용")
  private String notificationContent;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Comment("알림의 Type, Enum으로 정의")
  private NotificationType notificationType;
}
