package com.nyam.everyday.module.challenge.entity;

import com.nyam.everyday.common.entity.BaseCreatedEntity;
import com.nyam.everyday.module.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CollectionIdJdbcTypeCode;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberChallengeDay extends BaseCreatedEntity {

  @Id
  @Column(name = "member_challenge_day_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JoinColumn(name = "member_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  @JoinColumn(name = "challenge_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Challenge challenge;

  @Column(nullable = false)
  private LocalDate targetDate;
}
