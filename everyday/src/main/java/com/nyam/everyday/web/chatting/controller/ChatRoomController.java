package com.nyam.everyday.web.chatting.controller;

import com.nyam.everyday.module.chatting.chatroom.registry.ChatRoomRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@Tag(name = "Chat-Room-Test-Controller", description = "채팅방 관련 정보를 얻기 위한 테스트 컨트롤러")
@RequestMapping("/api/chatroom")
public class ChatRoomController {

  private final ChatRoomRegistry chatRoomRegistry;

  @GetMapping("/{teamId}")
  @Operation(summary = "특정 채팅방 참여자의 목록을 불러오는 findSubscribers 함수 테스트를 위한 컨트롤")
  public ResponseEntity<List<Long>> getChattingMembers(@PathVariable Long teamId) {
    List<Long> result = chatRoomRegistry.findSubscribers(teamId);

    return ResponseEntity.ok(result);
  }

}