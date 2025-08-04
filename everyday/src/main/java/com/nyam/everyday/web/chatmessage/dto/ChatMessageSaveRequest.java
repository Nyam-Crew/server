package com.nyam.everyday.web.chatmessage.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageSaveRequest {
  private String sender;
  private String content;
}
