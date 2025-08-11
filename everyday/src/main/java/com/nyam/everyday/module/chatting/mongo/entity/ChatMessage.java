package com.nyam.everyday.module.chatting.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// @Entity가 아니라 @Document를 사용하면 JPA에서 사용하는 것처럼 MongoDB 사용 가능
@Document(collection = "chat_message")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

  // @Document가 붙은 클래스 내의 @Id가 붙은 컬럼은 MongoDB가 알아서 값을 생성해준다.
  @Id
  private Long messageId;
  private Long roomId;
  private Long memberId;
  private String content;
  private Long timestamp;
}

///  위처럼 Entity를 작성하면, 저장시에는 아래와 같은 값이 됩니다
//  {
//    "_id": "66bfa7e25d18d135d776b6df",
//      "sender": "user123",
//      "content": "안녕하세요!"
//      "timestamp" : 12312423
//  }