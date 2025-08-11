package com.nyam.everyday.config.websocket.handler;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 * WebSocket Handshake 단계에서 세션의 사용자(Principal)를 결정하는 핸들러입니다.
 *
 * [무엇을 하나요]
 * - HTTP → WebSocket 업그레이드가 진행될 때 호출되어, 이 연결을 소유한 사용자를 표시할 Principal을 만듭니다.
 * - 여기서 설정한 Principal.getName() 값은 이후 STOMP에서 `/user/**` 경로 라우팅의 기준이 됩니다.
 *   예) SimpMessagingTemplate.convertAndSendToUser("123", "/queue/notice", payload)
 *
 * [전제 조건]
 * - 보통 HandshakeInterceptor.beforeHandshake()에서 JWT 검증 후 attributes에 userId를 담아둡니다.
 *   이 핸들러는 그 값을 읽어 Principal로 바꿉니다.
 *
 * [왜 필요한가]
 * - 유저별 1:1 전송(`/user/queue/**`)을 정확하게 하기 위해 세션의 사용자 식별값을 표준 방식으로 설정합니다.
 * - Controller의 @MessageMapping 메서드에서 Principal을 주입받아 손쉽게 사용자 식별이 가능합니다.
 *
 * [연결]
 * - WebSocketConfig.registerStompEndpoints()에서
 *   registry.addEndpoint("/ws").setHandshakeHandler(new CustomHandshakeHandler()) ... 처럼 등록합니다.
 *
 * [주의]
 * - attributes에서 꺼내는 키 이름("userId")은 Interceptor에서 put한 키와 동일해야 합니다.
 * - 권한이 필요하면 UsernamePasswordAuthenticationToken 생성 시 적절한 GrantedAuthority 목록을 넣으십시오.
 */
public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    /**
     * Handshake 시점에 이 WebSocket 연결의 Principal을 결정합니다.
     *
     * @param request    현재 HTTP 요청의 추상화입니다. (헤더, URI 등 조회 가능)
     * @param wsHandler  이 연결을 처리할 WebSocketHandler입니다. 보통 직접 사용하지 않습니다.
     * @param attributes HandshakeInterceptor가 beforeHandshake에서 담아둔 세션 속성 Map입니다.
     *                   예) attributes.put("userId", 123L)
     * @return           이 연결의 사용자 정보를 담은 Principal. 반환된 Principal.getName()이 `/user/**` 라우팅 키가 됩니다.
     */
    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        // HandshakeInterceptor에서 저장해 둔 사용자 ID를 꺼냅니다. 예) 123L
        Object userId = attributes.get("userId");           // 인터셉터에서 넣은 값
        // Principal 이름으로 사용할 문자열로 변환합니다. STOMP의 convertAndSendToUser에서 이 값을 사용합니다.
        String name = String.valueOf(userId);               // "123"
        // 사용자 이름(name)만 담은 Principal을 생성해 반환합니다.
        // 두 번째 인자(credentials)는 사용하지 않으므로 null, 세 번째 인자(authorities)는 빈 목록입니다.
        // 역할/권한 기반 라우팅이나 보안이 필요하다면 authorities에 ROLE_* 권한을 넣으세요.
        return new UsernamePasswordAuthenticationToken(name, null, List.of());
    }
}
