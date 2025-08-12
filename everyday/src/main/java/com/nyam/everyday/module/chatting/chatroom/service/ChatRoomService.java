package com.nyam.everyday.module.chatting.chatroom.service;

import com.nyam.everyday.module.chatting.chatroom.entity.ChatRoom;
import com.nyam.everyday.module.chatting.chatroom.repository.ChatRoomRepository;
import com.nyam.everyday.module.team.service.TeamMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatRoomService {

  private final ChatRoomRepository chatRoomRepository;
  private final TeamMemberService teamMemberService;

  // 어떤 유저가 특정 채팅방에 들어갈 권한이 있는지를 판단한다
  public boolean authCheck(Long memberId, Long chatRoomId) {
    ChatRoom chatRoom = chatRoomRepository.getByChatRoomId(chatRoomId);
    Long teamId = chatRoom.getTeam().getTeamId();

    return teamMemberService.isMember(memberId, teamId);
  }

}
