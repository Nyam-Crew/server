package com.nyam.everyday.web.member.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "회원 정보 요청")
public class MemberRequestDto {

  @Schema(description = "아이디")
  private Long memberId;

  @Schema(description = "회원 닉네임")
  private String nickname;

  @Schema(description = "이메일")
  private String email;

  @Schema(description = "회원 프로필 이미지 주소.(S3 url)")
  private String memberImg;

  @Schema(description = "회원 성별", allowableValues = {"U", "M", "F"}, example = "U" )
  private String gender;

  @Schema(description = "회원 키")
  private float height;

  @Schema(description = "회원 몸무게")
  private float weight;

  @Schema(description = "회원 나이")
  private int age;

  @Schema(description = "활동레벨", allowableValues = { "SEDENTARY", "LIGHT", "MODERATE", "ACTIVE", "VERY_ACTIVE"}, example = "MODERATE")
  private String activityLevel;

}
