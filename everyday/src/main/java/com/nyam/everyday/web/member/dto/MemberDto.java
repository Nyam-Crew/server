package com.nyam.everyday.web.member.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "회원 정보")
public class MemberDto {

  @Schema(description = "아이디")
  private Long memberId;

  @Schema(description = "회원아이디") //@kakao
  private String providerId;

  @Schema(description = "회원 닉네임")
  private String nickname;

  @Schema(description = "회원 프로필 이미지")
  private String memberImg;

  @Schema(description = "회원 성별")
  private String gender;

  @Schema(description = "회원 성별")
  private float height;

  @Schema(description = "회원 몸무게")
  private float weight;

  @Schema(description = "회원 나이")
  private int age;


  @Schema(description = "이메일")
  private String email;

  @Schema(description = "회원 생성일")
  private LocalDateTime createdDate;

  @Schema(description = "회원정보 수정일")
  private LocalDateTime modifiedDate;
}
