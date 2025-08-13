package com.nyam.everyday.web.chatting.controller;

import com.nyam.everyday.module.chatting.chatmessage.service.ChatMessageService;
import com.nyam.everyday.module.chatting.chatroom.service.ChatRoomService;
import com.nyam.everyday.web.chatting.dto.ChatMessageSaveRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Tag(name = "Chat-Socket-Controller", description = "소켓으로 들어온 채팅 요청을 처리하기 위한 컨트롤러")
@MessageMapping("/chat")
public class ChatSocketController {

  private final ChatMessageService chattingService;


  // 특정 채팅방에 메세지를 보내기 전에 미리 권한 체크
  @PreAuthorize("@chatRoomService.authCheck(#roomId, T(java.lang.Long).parseLong(principal.name()))")
  @MessageMapping("/{roomId}")
  public void handleMessage(
      @DestinationVariable Long roomId,
      @Payload ChatMessageSaveRequest request,
      Principal principal
  ) {
    Long memberId = Long.parseLong(principal.getName());

    chattingService.handleMessage(request, memberId);
  }
}