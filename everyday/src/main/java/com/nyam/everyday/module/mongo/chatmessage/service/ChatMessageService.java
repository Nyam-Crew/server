package com.nyam.everyday.module.mongo.chatmessage.service;

import com.nyam.everyday.module.mongo.chatmessage.entity.ChatMessage;
import com.nyam.everyday.module.mongo.chatmessage.repository.ChatMessageRepository;
import com.nyam.everyday.web.chatmessage.dto.ChatMessageSaveRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

  private final ChatMessageRepository chatMessageRepository;

  public ChatMessage save(ChatMessageSaveRequest chatMessageSaveRequest) {
    ChatMessage chatMessage = ChatMessage.builder().sender(chatMessageSaveRequest.getSender())
        .content(chatMessageSaveRequest.getContent()).timestamp(System.currentTimeMillis()).build();

    return chatMessageRepository.save(chatMessage);
  }

  public List<ChatMessage> findBySender(String sender) {
    return chatMessageRepository.findBySender(sender);
  }
}
