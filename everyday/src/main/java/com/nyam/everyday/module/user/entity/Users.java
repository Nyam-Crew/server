package com.nyam.everyday.module.user.entity;

import com.nyam.everyday.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "users")
@Builder
public class Users  extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Comment("아이디")
  private Long id;

  @Comment("회원 아이디")
  @Column(nullable = false, unique = true)
  private String userId;

  @Comment("닉네임")
  @Column
  private String nickname;

  @Comment("이메일")
  @Column
  private String email;

}
