package com.nyam.everyday.module.challenge.entity;

import com.nyam.everyday.common.entity.BaseCreatedEntity;
import com.nyam.everyday.module.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class MemberChallengeStatus extends BaseCreatedEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long mcs_id;

  @Comment("member 테이블의 PK")
  @JoinColumn(nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  @Comment("challenge 테이블의 PK")
  @JoinColumn(nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Challenge challenge;
}
