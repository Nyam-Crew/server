package com.nyam.everyday.module.challenge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Challenge {

  @Id
  @Column(name = "challenge_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JoinColumn(name = "badge_id")
  @Column(nullable = false)
  @Comment("완료 시 전달될 Badge의 ID")
  private Long badgeId;

  @Comment("챌린지 이름")
  @Column(nullable = false, length = 30)
  private String title;

  @Comment("챌린지 설명")
  @Column(nullable = false, length = 255)
  private String description;

  @Enumerated(EnumType.STRING)
  @Comment("챌린지 코드")
  @Column(nullable = false, length = 50)
  private ChallengeCode challengeCode;

  @Comment("챌린지 타입")
  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private ChallengeType type;

  @Comment("달성을 위해 필요한 횟수")
  @Column(nullable = false)
  private Long targetCount;

  @Comment("챌린지 시작일, 지정 없으면 항상 하는 챌린지")
  @Builder.Default
  private LocalDateTime startDate = null;

  @Comment("챌린지 종료일, 지정 없으면 항상 하는 챌린지")
  @Builder.Default
  private LocalDateTime endDate = null;
}
