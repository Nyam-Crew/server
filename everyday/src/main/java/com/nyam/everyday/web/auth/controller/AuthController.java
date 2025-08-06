package com.nyam.everyday.web.auth.controller;

import com.nyam.everyday.module.auth.service.AuthService;
import com.nyam.everyday.oauth2.OAuth2LogoutSuccessHandler;
import com.nyam.everyday.web.auth.dto.LoginRequestDto;
import com.nyam.everyday.web.auth.dto.LoginResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Auth-Controller", description = "권한관리")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final OAuth2LogoutSuccessHandler oAuth2LogoutSuccessHandler;

  private final AuthService authService;

  @Operation(summary = "로그인", description = "사용자 인증 후 토큰을 발급합니다")
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDTO) {
    LoginResponseDto loginResponseDTO = authService.login(loginRequestDTO);
    return ResponseEntity.ok(loginResponseDTO);
  }

  @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다")
  @PostMapping("/refresh")
  public ResponseEntity<?> refreshToken(@RequestHeader(value = "Authorization", required = false)
  String authorizationHeader, HttpServletRequest request) {
    String refreshToken = null;
    // 1. 쿠키에서 찾기
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("refreshToken".equals(cookie.getName())) {
          refreshToken = cookie.getValue();
        }
      }
    }
    // 2. Authorization 헤더 찾기
    if (refreshToken == null && authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      refreshToken = authorizationHeader.replace("Bearer ", "").trim();
    }
    if (refreshToken == null || refreshToken.isEmpty()) {
      throw new IllegalArgumentException("리프레시 토큰이 없습니다.");
    }
    String newAccessToken = authService.refreshToken(refreshToken);
    Map<String, String> res = new HashMap<>();
    res.put("accessToken", newAccessToken);
    res.put("refreshToken", refreshToken);

    return ResponseEntity.status(HttpStatus.OK).body(res);
  }

  @Operation(summary = "로그아웃", description = "사용자의 인증 토큰을 무효화하고 쿠키를 삭제합니다")
  @PostMapping("/logout")
  public  ResponseEntity<String> logout(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    try {
      oAuth2LogoutSuccessHandler.onLogoutSuccess(request, response, authentication);
    } catch (Exception e) {
      log.error("로그아웃 성공 핸들러 처리 중 예외 발생: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().body("로그아웃 처리 중 오류가 발생했습니다.");
    }
    return ResponseEntity.ok().body("로그아웃 완료(쿠키삭제)");
  }
}
