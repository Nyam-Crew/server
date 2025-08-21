package com.nyam.everyday.config.websocket.interceptor;

import com.nyam.everyday.security.jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

/// 지금은 사용하지 않는 Interceptor입니다.
@Component
@Slf4j
@RequiredArgsConstructor
public class StompAuthConnectInterceptor implements ChannelInterceptor {

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);

    // CONNECT 때만 인증
    if (StompCommand.CONNECT.equals(acc.getCommand())) {
      log.info("StompAuthConnectionInterceptor : Connect 프레임 받음");

      // 1) STOMP 네이티브 헤더에서 Authorization 읽기
      String auth = acc.getFirstNativeHeader("Authorization");
      if (auth == null || !auth.startsWith("Bearer ")) {

        throw new AccessDeniedException("토큰이 유효하지 않습니다");
      }

      String token = auth.substring(7);

      // 2) 토큰 검증
      if (!jwtTokenProvider.validateToken(token)) {
        throw new AccessDeniedException("토큰이 유효하지 않습니다");
      }

      log.info("StompAuthConnectionInterceptor : 토큰 인증 완료");

      // 3) 사용자 정보 추출
      String userId = jwtTokenProvider.getUserIdFromToken(token).toString();

      // 4) 현재 STOMP 세션에 Principal 설정
      acc.setUser(new UsernamePasswordAuthenticationToken(userId, null, List.of()));

      // 5) 필요하면 세션 속성에 부가정보 저장
      Map<String, Object> attrs = acc.getSessionAttributes();
      if (attrs != null) {
        attrs.put("userId", userId);
      }
      log.info("StompAuthConnectionInterceptor : 토큰 기반으로 유저 정보 저장 완료, 유저 아이디는 {}", userId);
    }

    return message;
  }
}
