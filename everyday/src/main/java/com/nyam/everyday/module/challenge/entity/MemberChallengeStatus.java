package com.nyam.everyday.module.challenge.entity;

import com.nyam.everyday.common.entity.BaseEntity;
import com.nyam.everyday.module.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberChallengeStatus extends BaseEntity {

  @Id
  @Column(name = "member_challenge_status_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Comment("member 테이블의 PK")
  @JoinColumn(name = "member_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  @Comment("challenge 테이블의 PK")
  @JoinColumn(name = "challenge_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Challenge challenge;

  @Column(nullable = false)
  @Builder.Default
  private Boolean isCleared = false;

  @Column(nullable = false)
  @Builder.Default
  @Setter
  private Long progressCount = 0L;

  // 클리어 처리할 떄 사용
  public void setAsCleared() {
    this.isCleared = true;
  }
}
