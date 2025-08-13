package com.nyam.everyday.web.chatting.dto;

import com.nyam.everyday.module.chatting.chatmessage.mongo.entity.ChatMessage;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageBroadcastDto {

  private String messageId;
  private Long senderId;
  private String sender;
  private String content;
  private Long timestamp;

  public static ChatMessageBroadcastDto of(ChatMessage chatMessage) {
    return ChatMessageBroadcastDto.builder()
        .messageId(chatMessage.getMessageId())
        .senderId(chatMessage.getMemberId())
        .sender(chatMessage.getNickname() != null ? chatMessage.getNickname() : "닉네임 없음")
        .content(chatMessage.getContent())
        .timestamp(chatMessage.getTimestamp())
        .build();
  }
}
