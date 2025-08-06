package com.nyam.everyday.web.auth.dto;

import com.nyam.everyday.module.auth.entity.Auth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 응답 DTO")
public class LoginResponseDto {

  @Schema(description = "토큰 타입", example = "Bearer")
  private String tokenType;

  @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzXVCJ9...")
  private String accessToken;

  @Schema(description = "리프레시 토큰", example = "eyJhbGciXVCJ9...")
  private String refreshToken;

  @Schema(description = "사용자 ID", example = "1")
  private Long userId;

  public LoginResponseDto(Auth auth) {
    this.tokenType = auth.getTokenType();
    this.accessToken = auth.getAccessToken();
    this.refreshToken = auth.getRefreshToken();
    this.userId = auth.getMember().getMemberId();
  }
}
