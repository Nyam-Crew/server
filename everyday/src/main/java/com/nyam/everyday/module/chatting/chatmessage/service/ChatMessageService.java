package com.nyam.everyday.module.chatting.chatmessage.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.module.chatting.chatmessage.mongo.entity.ChatMessage;
import com.nyam.everyday.module.chatting.chatmessage.mongo.repository.ChatMessageRepository;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.team.enums.ParticipationStatus;
import com.nyam.everyday.module.team.repository.TeamMemberStatusRepository;
import com.nyam.everyday.web.chatting.dto.ChatMessageBroadcastDto;
import com.nyam.everyday.web.chatting.dto.ChatMessageSaveRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatMessageService {

  private final ChatMessageRepository chatMessageRepository;
  private final TeamMemberStatusRepository teamMemberStatusRepository;
  private final SimpMessagingTemplate simpMessagingTemplate;
  private final MemberRepository memberRepository;
  private final MongoTemplate mongoTemplate;

  // 메세지가 전송되었을 때 처리하기 위한 메서드
  public void handleMessage(ChatMessageSaveRequest request, Long memberId) {
    // 보낸 멤버 정보를 가져오고, 없으면 에러
    log.info("[handleMessage] : 메세지 수신");
    Member member = memberRepository.findByMemberId(memberId).orElseThrow(() -> BaseException.MEMBER_NOT_FOUND);

    log.info("[handleMessage] : 보낸 유저는 {}, 방 번호는 {}", member.getNickname(), request.getRoomId());

    // 저장을 위한 정보 저장
    ChatMessage chatMessage = ChatMessage.builder()
        .memberId(memberId)
        .nickname(member.getNickname() != null ? member.getNickname() : "닉네임 없음")
        .roomId(request.getRoomId())
        .content(request.getContent())
        .timestamp(System.currentTimeMillis())
        .build();

    // 먼저 메세지를 저장한다.
    ChatMessage saved = mongoTemplate.insert(chatMessage);
    // ChatMessage result = chatMessageRepository.save(chatMessage);
    log.info("[handleMessage] : 메세지 MongoDB에 저장 완료");

    // redis 캐시 갱신 (20개만 남게)₩

    // 저장 후에 Broadcast를 위한 자료형 준비
    ChatMessageBroadcastDto broadcast = ChatMessageBroadcastDto.builder()
        .messageId(saved.getMessageId())
        .senderId(saved.getMemberId())
        .sender(saved.getNickname())
        .content(saved.getContent())
        .timestamp(saved.getTimestamp())
        .build();

    // Broadcast 수행
    simpMessagingTemplate.convertAndSend("/topic/chat/" + request.getRoomId(), broadcast);
    log.info("[handleMessage] : Broadcast 완료");
  }

  // 내가 보낸 메세지 리스트를 얻기 위한 메서드
  public List<ChatMessage> getMyMessages(Long memberId) {
    return chatMessageRepository.findAllByMemberId(memberId);
  }

  // 특정 채팅방의 메세지 히스토리 받아오기
  @Transactional(readOnly = true)
  public List<ChatMessageBroadcastDto> getMessageHistory(Long memberId, Long roomId) {
     // 여기서 이 유저가 채팅방에 접근할 권한 있는지 체크
    if (!teamMemberStatusRepository.existsByTeam_TeamIdAndMember_MemberIdAndStatus(roomId, memberId,
        ParticipationStatus.APPROVED)) {
      log.info("{} 멤버는 {}번 채팅방의 내용에 접근할 수 없습니다", memberId, roomId);
      throw BaseException.ACCESS_DENIED;
    }

    // Redis에 저장된 값 있는지 체크

    List<ChatMessage> messages = chatMessageRepository.findAllByRoomId(roomId);
    List<ChatMessageBroadcastDto> result = new ArrayList<>();

    // 리스트화해서 반환
    for (ChatMessage chatMessage : messages) {
      result.add(ChatMessageBroadcastDto.of(chatMessage));
      log.info("{}가 result에 더해짐", chatMessage);
    }


    log.info("[ChatMessageService] - getMessageHistory : 반환 결과는 {}", result);
    return result;
  }
}
