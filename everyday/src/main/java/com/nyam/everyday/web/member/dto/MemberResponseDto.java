package com.nyam.everyday.web.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "회원 정보")
public class MemberResponseDto {

  @Schema(description = "아이디")
  private Long memberId;
/*
  @Schema(description = "회원아이디") //@kakao
  private String providerId;*/

  @Schema(description = "회원 닉네임")
  private String nickname;

  @Schema(description = "이메일")
  private String email;

  @Schema(description = "회원 프로필 이미지 주소.(S3 url)")
  private String memberImg;

  @Schema(description = "회원 프로필 이미지 파일(파일업로드)")
  private MultipartFile memberImgFile;

  @Schema(description = "회원 성별", allowableValues = {"U", "M", "F"}, example = "U" )
  private String gender;

  @Schema(description = "회원 키")
  private BigDecimal height;

  @Schema(description = "회원 몸무게")
  private BigDecimal weight;

  @Schema(description = "회원 나이")
  private int age;

  @Schema(description = "체질량지수")
  private BigDecimal bmi;

  @Schema(description = "기초대사량")
  private BigDecimal bmr;

  @Schema(description = "총 일일 에너지 소비량")
  private BigDecimal tdee;

  @Schema(description = "활동레벨")
  private String activityLevel;

  @Schema(description = "회원상태")
  private String memberStatus;

  @Schema(description = "회원 생성일", example = "2025-01-01T00:00:00")
  private LocalDateTime createdDate;

  @Schema(description = "회원정보 수정일", example = "2025-01-01T00:00:00")
  private LocalDateTime modifiedDate;
}
