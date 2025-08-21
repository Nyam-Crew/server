package com.nyam.everyday.config.websocket.interceptor;

import com.nyam.everyday.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * [이 클래스는 무엇을 하나요?]
 * - 웹브라우저가 서버와 WebSocket 연결을 맺기 "직전"에 요청을 가로채서
 *   접속을 허용할지 거부할지 판단합니다. 이런 역할을 하는 친구를 "핸드셰이크 인터셉터"라고 합니다.
 *
 * [왜 필요할까요?]
 * - WebSocket 연결은 한 번 열리면 오래 유지됩니다.
 *   그래서 연결 직전에 토큰이나 쿠키를 검사해 인증되지 않은 사용자를 차단하는 것이 중요합니다.
 *
 * [어떻게 동작하나요?]
 *  1) beforeHandshake()
 *     - 아직 HTTP 단계라서 요청/응답에 접근할 수 있습니다.
 *     - 헤더·쿠키·쿼리에서 토큰을 읽고 검증합니다.
 *     - 세션 속성 Map(attributes)에 사용자 정보를 넣어두면
 *       이후 WebSocket 세션에서도 그 값을 사용할 수 있습니다.
 *     - true를 반환하면 업그레이드 진행, false면 연결 거부입니다.
 *  2) afterHandshake()
 *     - 업그레이드가 끝난 뒤 한 번 호출됩니다.
 *     - 로깅 같은 후처리를 넣을 때 사용합니다.
 *
 * [주의할 점]
 * - SockJS 환경에서는 브라우저나 프록시 영향으로 Authorization 헤더가 빠질 수 있습니다.
 *   이럴 때는 쿼리 파라미터(?token=...)로도 받는 방식을 함께 고려합니다.
 * - 아래 jwtTokenProvider 주입 방법을 꼭 점검하세요. (TODO 주석 참고)
 */

// @Component: 스프링이 이 클래스를 빈으로 등록합니다.
// @Slf4j : 수행 중 발생한 상황을 로그로 출력하기 위해 등록합니다.
// @RequiredArgsConstructor: final 또는 @NonNull 필드를 생성자로 받아 자동 주입합니다.
@Component
@Slf4j
@RequiredArgsConstructor
public class CustomHandshakeInterceptor implements HandshakeInterceptor {

  // JWT 토큰을 검증하고 사용자 식별값을 꺼내는 컴포넌트

  private final JwtTokenProvider jwtTokenProvider;

  // Handshake 직전에 실행됩니다.
  @Override
  public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
      WebSocketHandler wsHandler, Map<String, Object> attributes) {

    // 요청 객체가 Servlet 기반이면, HttpServletRequest로 바꿔서 헤더/쿠키/세션에 접근합니다.
    if (request instanceof ServletServerHttpRequest servletRequest) {

      // 원본 서블릿 요청 꺼내기
      HttpServletRequest req = servletRequest.getServletRequest();

      // 쿠키에서 값 꺼내기
      String token = extractAccessTokenFromCookies(req.getCookies(), "accessToken").orElse(null);
      log.info("[BeforeHandshake] : 쿠키에서 값 꺼내기 성공 : {}", token);

      // 토큰 없으면, 연결 거부
      if (token == null) {
        log.info("[BeforeHandshake] : 쿠키에 값이 없어서 연결을 해제합니다.");
        return false;
      }

      // 토큰이 유효하지 않아도 연결 거부
      if (!jwtTokenProvider.validateToken(token)) {
        log.info("[BeforeHandshake] : 쿠키 값이 유효하지 않아서 연결을 해제합니다.");
        return false;
      }

      Long memberId = jwtTokenProvider.getUserIdFromToken(token);
      log.info("[BeforeHandshake] : 인증에 성공했습니다. userId는 {}", memberId);
      // HandshakeHandler에서 principal을 만들 수 있도록, 값을 담아둔다.
      attributes.put("memberId", memberId);
      return true;
    }

    return false;
  }

  @Override
  public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
      WebSocketHandler wsHandler, Exception exception) {
    // 업그레이드가 끝난 뒤 호출됩니다.
    // 여기서는 로깅이나 모니터링 같은 후처리를 할 수 있습니다.
    // 실패한 경우 exception으로 이유를 확인할 수 있습니다.
  }

  // Cookie에서 AccessToken 꺼내기
  private Optional<String> extractAccessTokenFromCookies(Cookie[] cookies, String name) {
    if (cookies == null) return Optional.empty();
    for (Cookie c : cookies) {
      if (name.equals(c.getName())) {
        return Optional.ofNullable(c.getValue());
      }
    }
    return Optional.empty();
  }
}