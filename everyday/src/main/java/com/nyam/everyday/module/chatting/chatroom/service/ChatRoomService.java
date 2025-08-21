package com.nyam.everyday.module.chatting.chatroom.service;

import com.nyam.everyday.module.team.service.TeamMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatRoomService {

  private final TeamMemberService teamMemberService;

  // 어떤 유저가 특정 채팅방에 들어갈 권한이 있는지를 판단한다
  public boolean authCheck(Long memberId, Long teamId) {

    return teamMemberService.isMember(memberId, teamId);
  }
}
