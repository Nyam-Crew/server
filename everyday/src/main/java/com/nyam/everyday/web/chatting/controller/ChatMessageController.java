package com.nyam.everyday.web.chatting.controller;

import com.nyam.everyday.module.chatting.chatmessage.mongo.entity.ChatMessage;
import com.nyam.everyday.module.chatting.chatmessage.service.ChatMessageService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.chatting.dto.ChatMessageBroadcastDto;
import com.nyam.everyday.web.chatting.dto.ChatMessageSaveRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat-Message-Controller", description = "채팅 저장을 위한 임시 컨트롤러")
@RequiredArgsConstructor
public class ChatMessageController {

  private final ChatMessageService chatMessageService;

  @PostMapping("/{teamId}")
  @Operation(summary = "채팅 전송받기")
  public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatMessageSaveRequest request,
      @PathVariable Long teamId,
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long memberId = customUserDetails.getId();

    ChatMessage chatMessage = chatMessageService.handleMessage(request, memberId, teamId);

    return ResponseEntity.ok(chatMessage);
  }

  @GetMapping("/history/{teamId}")
  @Operation(summary = "특정 채팅방의 과거 메세지 불러오기")
  public ResponseEntity<List<ChatMessageBroadcastDto>> getMessageHistory(
      @PathVariable("teamId") Long teamId,
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long memberId = customUserDetails.getId();

    return ResponseEntity.ok(chatMessageService.getMessageHistory(memberId, teamId));
  }

  @GetMapping("/history/{teamId}/all")
  @Operation(summary = "특정 채팅방에 과거 메세지 전체 불러오기")
  public ResponseEntity<List<ChatMessageBroadcastDto>> getAllMessageHistory(
      @PathVariable("teamId") Long teamId,
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long memberId = customUserDetails.getId();

    return ResponseEntity.ok(chatMessageService.getAllMessageHistory(memberId, teamId));
  }
}
