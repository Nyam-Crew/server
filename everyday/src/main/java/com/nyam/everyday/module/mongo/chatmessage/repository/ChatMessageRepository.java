package com.nyam.everyday.module.mongo.chatmessage.repository;


import com.nyam.everyday.module.mongo.chatmessage.entity.ChatMessage;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

  List<ChatMessage> findBySender(String sender);
}
