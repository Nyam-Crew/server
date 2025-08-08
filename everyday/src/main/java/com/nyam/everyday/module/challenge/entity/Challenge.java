package com.nyam.everyday.module.challenge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long challengeId;

  @Comment("챌린지 이름")
  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private ChallengeTitle challengeTitle;

  @Comment("챌린지 설명")
  @Column(nullable = false, length = 255)
  private String challengeContent;

  @Comment("챌린지 시작일, 지정 없으면 항상 하는 챌린지")
  @Column(nullable = false)
  @Builder.Default
  private LocalDateTime challengeStartDate = LocalDateTime.of(2000, 1, 1, 0, 0);

  @Comment("챌린지 종료일, 지정 없으면 항상 하는 챌린지")
  @Column(nullable = false)
  @Builder.Default
  private LocalDateTime challengeEndDate = LocalDateTime.of(2099, 12, 31, 0, 0);

  @Comment("챌린지 타입")
  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private ChallengeType challengeType;
}
