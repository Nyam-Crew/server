package com.nyam.everyday.web.chatting.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageSaveRequest {

  private String content;
}
