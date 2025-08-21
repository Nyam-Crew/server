package com.nyam.everyday.module.chatting.chatroom.registry;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomRegistry {

  private final SimpUserRegistry userRegistry;

  // 특정 채팅방에 접속중인 유저의 리스트를 받는다
  public List<Long> findSubscribers(String teamId) {
    String destination = "/topic/chat/" + teamId;
    List<Long> result = new ArrayList<>();
    for (SimpUser user : userRegistry.getUsers()) {
      for (SimpSession session : user.getSessions()) {
        for (SimpSubscription sub : session.getSubscriptions()) {
          if (destination.equals(sub.getDestination())) {
            log.info("{}가 구독중이라 리스트에 추가되었습니다.", user.getName());
            result.add(Long.parseLong(user.getName())); // sub.getId(), sub.getDestination(), sub.getSession().getId(), user.getName() 등 활용
          }
        }
      }
    }

    return result;
  }
}
