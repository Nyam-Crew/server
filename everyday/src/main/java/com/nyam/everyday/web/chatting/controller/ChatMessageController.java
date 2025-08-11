package com.nyam.everyday.web.chatting.controller;

import com.nyam.everyday.module.chatting.mongo.entity.ChatMessage;
import com.nyam.everyday.module.chatting.mongo.service.ChatMessageService;
import com.nyam.everyday.web.chatting.dto.ChatMessageSaveRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat-Message-Controller", description = "채팅 저장을 위한 임시 컨트롤러")
@RequiredArgsConstructor
public class ChatMessageController {

  private final ChatMessageService chatMessageService;

  @PostMapping("/{send}")
  @Operation(summary = "채팅 저장하기")
  public ChatMessage sendMessage(@RequestBody ChatMessageSaveRequest chatMessage) {
    return chatMessageService.save(chatMessage);
  }

  @GetMapping("/history/me")
  @Operation(summary = "내가 보냈던 메세지 목록 확인하기")
  public List<ChatMessage> getMyMessages(@AuthenticationPrincipal UserDetails userDetails) {
    return chatMessageService.getMyMessages(memberId);
  }

  @GetMapping("/history/{roomId}")
  @Operation(summary = "특정 채팅방의 과거 메세지 불러오기")
  public List<ChatMessage> getMessageHistory(@PathVariable("roomId") Long roomId) {
    return chatMessageService.getMessageHistory(roomId);
  }
}
