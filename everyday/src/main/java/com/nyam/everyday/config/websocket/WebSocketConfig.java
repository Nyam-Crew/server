package com.nyam.everyday.config.websocket;

import com.nyam.everyday.config.websocket.handler.CustomHandshakeHandler;
import com.nyam.everyday.config.websocket.interceptor.ClientNoBroadcastInterceptor;
import com.nyam.everyday.config.websocket.interceptor.CustomHandshakeInterceptor;
import com.nyam.everyday.config.websocket.interceptor.RoomSubscribeAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final RoomSubscribeAuthInterceptor roomSubscribeAuthInterceptor;
  private final CustomHandshakeHandler customHandshakeHandler;
  private final CustomHandshakeInterceptor customHandshakeInterceptor;
  private final ClientNoBroadcastInterceptor clientNoBroadcastInterceptor;

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws") // 클라이언트가 연결할 엔드포인트
        .setAllowedOrigins("http://localhost:5173") // CORS 허용
        .setHandshakeHandler(customHandshakeHandler)  // 직접 생성한 Handler 추가
        .addInterceptors(customHandshakeInterceptor);  // 직접 생성한 Interceptor 추가
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/app"); // 클라이언트가 메시지 보낼 때 prefix
    registry.enableSimpleBroker("/topic", "/queue"); // 서버가 메시지 보낼 때 prefix
    registry.setUserDestinationPrefix("/user"); // 사용자별 prefix
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(
        roomSubscribeAuthInterceptor,
        clientNoBroadcastInterceptor
    );
  }
}