package com.nyam.everyday.module.member.entity;

import com.nyam.everyday.common.entity.BaseEntity;
import com.nyam.everyday.common.entity.Gender;
import com.nyam.everyday.common.entity.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "member")
@Builder
public class Member extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Comment("회원 아이디")
  private Long member_id;

  @Comment("소셜 아이디")
  @Column(nullable = false, unique = true)
  private String provider_id;

  @Comment("닉네임")
  @Column(nullable = false, unique = true)
  private String nickname;

  @Comment("회원 사진")
  @Column(nullable = false)
  private String member_img;

  @Comment("성별")
  @Column(nullable = false)
  private Gender gender;

  @Comment("키")
  @Column(nullable = false)
  private BigDecimal height;

  @Comment("몸무게")
  @Column(nullable = false)
  private BigDecimal weight;

  @Comment("나이")
  @Column(nullable = false)
  private int age;

  @Comment("회원상태")
  @Column(nullable = false)
  private Status member_status = Status.ACTIVATED;

}
