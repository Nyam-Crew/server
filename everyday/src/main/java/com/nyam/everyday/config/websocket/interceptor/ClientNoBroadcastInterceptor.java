package com.nyam.everyday.config.websocket.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ClientNoBroadcastInterceptor implements ChannelInterceptor {

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);
    log.info("[ClientNoBroadcastInterceptor] : 동작함 현재 프레임은 {}", acc.getCommand());

    if (StompCommand.SEND.equals(acc.getCommand())) {
      log.info("[ClientNoBroadcastInterceptor] : 전송 요청 받음");

      String dest = acc.getDestination();
      log.info("[ClientNoBroadcastInterceptor] : dest = {}", dest);

      if (dest != null && (dest.startsWith("/topic/") || dest.startsWith("/queue/"))) {
        log.warn("[ClientNoBroadcastInterceptor] : 차단 클라이언트가 브로커 목적지로 SEND 시도 dest = {}", dest);
        throw new AccessDeniedException("클라이언트의 /topic, /queue 전송은 허용되지 않습니다");
      }

      log.info("[ClientNoBroadcastInterceptor] : 전송 요청을 수락했습니다");
    }

    return message;
  }
}
