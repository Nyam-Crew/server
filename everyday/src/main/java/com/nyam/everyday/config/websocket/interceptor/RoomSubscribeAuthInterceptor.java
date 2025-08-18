package com.nyam.everyday.config.websocket.interceptor;

import com.nyam.everyday.module.chatting.chatroom.service.ChatRoomService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

// ===========================================================================
// 이 파일은 무엇을 하나요?
// - 사용자가 "구독"(SUBSCRIBE) 요청을 보낼 때, 그 요청을 가로채어
//   해당 사용자가 특정 채팅방 토픽(예: "/topic/room/{방번호}")을
//   구독해도 되는지(권한이 있는지)를 확인합니다.
// - 권한이 없다고 판단되면 예외를 던져 구독을 막습니다. 그러면 클라이언트는
//   STOMP ERROR 프레임을 받아 화면에 에러로 표시할 수 있습니다.
//
// 언제 실행되나요?
// - 웹소켓 연결이 성립한 뒤, 사용자가 어떤 토픽을 "구독"하려는 순간마다 실행됩니다.
//   (STOMP 프로토콜의 SUBSCRIBE 프레임이 들어올 때)
//
// 왜 필요한가요?
// - 누구나 아무 방이나 구독하면 안 되기 때문입니다. 이 인터셉터가 중간 문지기 역할을 합니다.
//
// 파일 위치(권장): config/websocket/interceptor/RoomSubscribeAuthInterceptor.java
// 의존 서비스: RoomMembershipService (module/room/service/...) — 사용자가 방 멤버인지 확인
// ===========================================================================

/**
 * [한눈에 보기] 사용자가 "/topic/room/{방번호}" 같은 토픽을 구독하려고 할 때, 정말 그 방을 볼 자격이 있는지 확인하는 가드(문지기)입니다.
 * <p>
 * [작동 흐름] 1) 클라이언트가 STOMP SUBSCRIBE 요청을 보냅니다. (예: "/topic/room/1") 2) 이 인터셉터가 요청을 가로챕니다. 3) 현재
 * 사용자(Principal)의 ID와 구독하려는 목적지(dest)를 읽습니다. 4) RoomMembershipService로 "해당 방의 멤버인가?"를 확인합니다. 5) 멤버가
 * 아니면 예외를 던져 구독을 차단합니다. (클라이언트는 ERROR 프레임 수신)
 * <p>
 * [중요 개념] - Principal: "이 연결은 누구의 것인가"를 나타내는 사용자 식별 정보입니다. HandshakeHandler 등에서 세팅된 값이 여기서
 * acc.getUser().getName()으로 도달합니다. - Destination(목적지): 구독하려는 토픽 경로입니다. (예: "/topic/room/1") -
 * RoomMembershipService: "사용자가 이 방의 멤버인가?"를 판단하는 도메인 서비스입니다.
 * <p>
 * [등록 방법] WebSocketConfig.configureClientInboundChannel(...)에서 registration.interceptors(...)로
 * 등록합니다.
 */

@Component // Spring이 자동으로 Bean으로 등록
@Slf4j // 로그 출력을 위해 사용
@RequiredArgsConstructor // final 필드(RoomMembershipService) 생성자 주입
public class RoomSubscribeAuthInterceptor implements ChannelInterceptor {

  // 서비스 계층: 실제 방 멤버 여부를 DB나 캐시에서 조회하는 로직 담당
  private final ChatRoomService chatRoomService;

  /**
   * 클라이언트가 서버로 메시지를 보내기 "직전"에 호출됩니다. 여기서는 SUBSCRIBE(구독) 요청만 골라서 권한을 체크합니다.
   *
   * @param message 클라이언트가 보낸 STOMP 프레임(헤더/바디 포함)
   * @param channel 내부 전달 채널(직접 사용할 일은 거의 없습니다)
   * @return 문제가 없으면 그대로 message를 반환하여 다음 단계로 진행합니다. 권한이 없으면 예외를 던져 구독을 차단합니다.
   */
  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    // STOMP 헤더를 쉽게 읽을 수 있도록 래퍼를 씌웁니다.
    StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);
//    log.info("[RoomSubscribeAuthInterceptor] : 동작함, 현재 프레임은 {}", acc.getCommand());

    // 이번 프레임이 "구독 요청"일 때만 검사합니다. (SEND/CONNECT 등은 패스)
    if (StompCommand.SUBSCRIBE.equals(acc.getCommand())) {

//      log.info("[RoomSubscribeAuthInterceptor] : 구독 요청 받음");

      // 사용자가 구독하려는 목적지(토픽) 경로. 예) "/topic/room/1"
      String dest = acc.getDestination();

      // 현재 연결의 사용자 식별자. HandshakeHandler 등에서 설정된 Principal의 이름을 사용합니다.
      Principal principal = acc.getUser();
      String memberId = principal.getName();

      // 이 예제에서는 방 토픽만 검사합니다. (다른 경로는 필요 시 추가)
      if (dest != null && dest.startsWith("/topic/chat/")) {

        // 목적지에서 방 번호만 깔끔하게 추출합니다. 예) "/topic/room/1" → "1"
        String roomId = dest.substring("/topic/chat/".length());

//        log.info("[RoomSubscribeAuthInterceptor] : 구독 요청 수신, 유저번호 : {}, 연결을 요청한 방 번호 : {}", memberId, roomId);

        // 핵심 권한 체크: 사용자 ID가 있고, 그 사용자가 해당 방의 멤버인지 확인합니다.
        boolean allowed = chatRoomService.authCheck(Long.parseLong(memberId),
            Long.parseLong(roomId));
//        log.info("[RoomSubscribeAuthInterceptor] : 권한 체크 함수 실행 완료. 결과는 {}", allowed);

        // 멤버가 아니면 구독을 막습니다. 클라이언트는 STOMP ERROR 프레임을 받고, 구독은 완료되지 않습니다.
        if (!allowed) {
//          log.info("[RoomSubscribeAuthInterceptor] : 멤버가 아니기에 구독 권한이 없습니다.");
          throw new AccessDeniedException("해당 방 구독 권한이 없습니다");
        }
      }

      // 권한에 문제가 없으면 원본 메시지를 그대로 반환하여 구독을 진행합니다.
//      log.info("[RoomSubscribeAuthInterceptor] : 구독 요청을 수락했습니다.");
    }

    return message;
  }

  // ------------------------------------------------------------------------
  // 어떻게 연결되나요?
  // - WebSocketConfig.configureClientInboundChannel(...)에서
  //   registration.interceptors(new RoomSubscribeAuthInterceptor(...)) 형태로 등록합니다.
  // - HandshakeHandler에서 Principal(사용자 ID)을 설정해 두어야 acc.getUser()가 정상 동작합니다.
  // ------------------------------------------------------------------------
}