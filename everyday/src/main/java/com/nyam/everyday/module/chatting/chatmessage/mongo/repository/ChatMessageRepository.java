package com.nyam.everyday.module.chatting.chatmessage.mongo.repository;


import com.nyam.everyday.module.chatting.chatmessage.mongo.entity.ChatMessage;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

  List<ChatMessage> findAllByMemberId(Long memberId);

  List<ChatMessage> findAllByRoomId(Long roomId);
}
