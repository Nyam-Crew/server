package com.nyam.everyday.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${aws.ec2ip.domain}")
  private String awsEc2IP;

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException exception) {

    String code = "oauth2_login_failed";
//    String message = "로그인에 실패했습니다.";

    // OAuth2AuthenticationException 이면 커스텀 코드 사용
    if (exception.getCause() instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException oae) {
      code = safe(oae.getError().getErrorCode(), "oauth2_login_failed");
//      message = safe(oae.getError().getDescription(), message);
    } else if (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException oae) {
      code = safe(oae.getError().getErrorCode(), "oauth2_login_failed");
//      message = safe(oae.getError().getDescription(), message);
    }


    // 절대 URL로 리다이렉트 (프론트로)
    try {
      String target = UriComponentsBuilder
          .fromUriString(awsEc2IP)     // e.g. "http://localhost:5173"
          .replacePath("/login")
          .queryParam("error", code)
          // .queryParam("message", message)
          .encode()
          .build()
          .toUriString();

      response.sendRedirect(target);
      return;
    } catch (Exception redirectEx) {
      log.warn("Redirect to frontend failed, falling back to JSON.", redirectEx);
    }

    // 실패 시 JSON fallback
    try {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      objectMapper.writeValue(response.getWriter(), Map.of("code", code));
    } catch (Exception e) {
      log.error("OAuth2 failure write response error", e);
    }
  }

  private static String safe(String v, String def) {
    return (v == null || v.isBlank()) ? def : v;
  }
}