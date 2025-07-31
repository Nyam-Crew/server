package com.nyam.everyday.web.user.dto;


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
public class UserDto {

  @Schema(description = "아이디")
  private Long id;

  @Schema(description = "회원아이디") //@kakao
  private String userId;

  @Schema(description = "회원 닉네임")
  private String nickname;

  @Schema(description = "이메일")
  private String email;

  @Schema(description = "회원 생성일")
  private LocalDateTime createdDate;

  @Schema(description = "회원정보 수정일")
  private LocalDateTime modifiedDate;
}
