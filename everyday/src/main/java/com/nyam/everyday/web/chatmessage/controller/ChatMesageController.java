package com.nyam.everyday.web.chatmessage.controller;

import com.nyam.everyday.module.mongo.chatmessage.entity.ChatMessage;
import com.nyam.everyday.module.mongo.chatmessage.service.ChatMessageService;
import com.nyam.everyday.web.chatmessage.dto.ChatMessageSaveRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat-Message-Controller", description = "채팅 저장을 위한 임시 컨트롤러")
@RequiredArgsConstructor
public class ChatMesageController {

  private final ChatMessageService chatMessageService;

@PostMapping("/send")
@Operation(summary = "채팅 저장하기", description = "")
  public ChatMessage sendMessage(@RequestBody ChatMessageSaveRequest chatMessage) {
    return chatMessageService.save(chatMessage);
  }

@GetMapping("/history")
  public List<ChatMessage> getMessageHistory(@RequestParam String sender) {
    return chatMessageService.findBySender(sender);
  }
}
