package com.nyam.everyday.module.chatting.chatroom.repository;

import com.nyam.everyday.module.chatting.chatroom.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  // 어떤 팀에 권한이 있어야 이 채팅창에 접근할 수 있는지 확인
  public ChatRoom getByChatRoomId(Long roomId);

}
