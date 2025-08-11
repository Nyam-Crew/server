package com.nyam.everyday.module.chatting.mongo.service;

import com.nyam.everyday.module.chatting.mongo.entity.ChatMessage;
import com.nyam.everyday.module.chatting.mongo.repository.ChatMessageRepository;
import com.nyam.everyday.web.chatting.dto.ChatMessageSaveRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatMessageService {

  private final ChatMessageRepository chatMessageRepository;

  public ChatMessage save(ChatMessageSaveRequest chatMessageSaveRequest) {
    ChatMessage chatMessage = ChatMessage.builder().memberId(chatMessageSaveRequest.)
        .content(chatMessageSaveRequest.getContent()).timestamp(System.currentTimeMillis()).build();

    return chatMessageRepository.save(chatMessage);
  }

  public List<ChatMessage> getMyMessages(Long memberId) {
    return chatMessageRepository.findAllByMemberId(memberId);
  }

  @Transactional(readOnly = true)
  public List<ChatMessage> getMessageHistory(Long roomId) {
    return chatMessageRepository.findAllByRoomId(roomId);
  }
}
