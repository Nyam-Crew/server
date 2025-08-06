package com.nyam.everyday.module.auth.entity;

import com.nyam.everyday.common.entity.BaseEntity;
import com.nyam.everyday.module.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Auth extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long authId;

  @Column(nullable = false)
  private String tokenType;

  @Column(nullable = false)
  private String accessToken;

  @Column(nullable = false)
  private String refreshToken;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="member_id")
  private Member member;

  public Auth(Member member, String refreshToken, String accessToken, String tokenType) {
    this.member = member;
    this.refreshToken = refreshToken;
    this.accessToken = accessToken;
    this.tokenType = tokenType;
  }

  public void updateAccessToken(String newAccessToken) {
    this.accessToken = newAccessToken;
  }

  public void updateRefreshToken(String newRefreshToken) {
    this.refreshToken = newRefreshToken;
  }


}
