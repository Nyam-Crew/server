package com.nyam.everyday.web.chatting.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageSaveRequest {
  private Long roomId;
  private String content;
}
