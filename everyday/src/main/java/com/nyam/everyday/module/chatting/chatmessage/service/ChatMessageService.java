package com.nyam.everyday.module.chatting.chatmessage.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.module.chatting.chatmessage.mongo.entity.ChatMessage;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.team.enums.ParticipationStatus;
import com.nyam.everyday.module.team.enums.TeamNotificationType;
import com.nyam.everyday.module.team.repository.TeamMemberStatusRepository;
import com.nyam.everyday.module.team.service.TeamNotificationService;
import com.nyam.everyday.redis.service.ChatHistoryCacheService;
import com.nyam.everyday.web.chatting.dto.ChatMessageBroadcastDto;
import com.nyam.everyday.web.chatting.dto.ChatMessageSaveRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatMessageService {

  private final TeamNotificationService teamNotificationService;
  private final ChatHistoryCacheService chatHistoryCacheService;
  private final TeamMemberStatusRepository teamMemberStatusRepository;
  private final SimpMessagingTemplate simpMessagingTemplate;
  private final MemberRepository memberRepository;
  private final MongoTemplate mongoTemplate;

  // 메세지가 전송되었을 때 처리하기 위한 메서드
  public ChatMessage handleMessage(ChatMessageSaveRequest request, Long memberId, Long teamId) {
    // 보낸 멤버 정보를 가져오고, 없으면 에러
    log.info("[handleMessage] : 메세지 수신");
    Member member = memberRepository.findByMemberId(memberId)
        .orElseThrow(() -> BaseException.MEMBER_NOT_FOUND);

    log.info("[handleMessage] : 보낸 유저는 {}, 방 번호는 {}", member.getNickname(), teamId);

    // 저장을 위한 정보 저장
    ChatMessage chatMessage = ChatMessage.builder()
        .memberId(memberId)
        .nickname(member.getNickname() != null ? member.getNickname() : "닉네임 없음")
        .teamId(teamId)
        .content(request.getContent())
        .timestamp(System.currentTimeMillis())
        .build();

    // 먼저 메세지를 저장한다.
    ChatMessage saved = mongoTemplate.insert(chatMessage);
    // ChatMessage result = chatMessageRepository.save(chatMessage);
    log.info("[handleMessage] : 메세지 MongoDB에 저장 완료");

    // 저장 후에 Broadcast를 위해 dto로 매핑
    ChatMessageBroadcastDto broadcast = ChatMessageBroadcastDto.of(saved);

    // redis 캐시 갱신 (20개만 남게)
    log.info("Redis 캐시 갱신 완료");
    chatHistoryCacheService.addMessage(teamId, broadcast);

    // Broadcast 수행
    simpMessagingTemplate.convertAndSend("/topic/chat/" + teamId, broadcast);
    log.info("[handleMessage] : Broadcast 완료");

    try {
      teamNotificationService.addTeamNotification(
              memberId,                   // 발신자 ID
              teamId,                     // 팀 ID
              TeamNotificationType.CHAT,  // 알림 타입
              "새로운 채팅이 발생하였습니다"        // 알림 내용 (채팅 메시지)
      );
    } catch (Exception e) {
      // 알림 실패가 채팅의 핵심 기능을 절대 중단시키지 않도록 보장
      log.error("[채팅 알림 생성 실패] 채팅 메시지는 정상 처리되었으나 알림 생성 중 예외 발생. teamId: {}. Error: {}",
              teamId, e.getMessage(), e);
    }

    return saved;
  }

  // 특정 채팅방에 처음 접속했을 때 사용한다. 첫 20개를 불러옴.
  // Redis에 먼저 가서 값을 찾고, 없다면 MongoDB에서 가져와서 캐싱한다
  @Transactional(readOnly = true)
  public List<ChatMessageBroadcastDto> getMessageHistory(Long memberId, Long teamId) {
    // 여기서 이 유저가 채팅방에 접근할 권한 있는지 체크
    this.authCheck(memberId, teamId);

    // Redis에 저장된 값 있는지 체크
    List<ChatMessageBroadcastDto> messages = chatHistoryCacheService.getMessages(teamId);
    // Redis에서 뭔가 불러오는 데에 성공?
    if (!messages.isEmpty()) {
      log.info("Redis에 값이 존재해서 그대로 반환");
      return messages;
    }

    // Redis에 값이 없다면, Mongo에서 불러와야 한다
    Query q = new Query();
    // roomId값이 입력된 roomId와 같은 값들을 가져온다.
    q.addCriteria(Criteria.where("teamId").is(teamId));
    // 최대 20개 뽑으며, 정렬기준은 timestamp의 내림차순
    q.limit(20).with(Sort.by(Sort.Direction.DESC, "timestamp"));
    // ChatMessage 클래스를 지정해줌으로써 어떤 컬렉션에서 가져올 지 정해준다.
    List<ChatMessage> result_mongo = mongoTemplate.find(q, ChatMessage.class);
    // TimeStamp의 DESC로 가져왔으니 역순이다. 뒤집어준다.
    Collections.reverse(result_mongo);

    // 저장할 배열 생성
    List<ChatMessageBroadcastDto> result = new ArrayList<>();

    // 각 값을 ChatMessage -> ChatMessageBroadCastDto로 변경
    for (ChatMessage chatMessage : result_mongo) {
      // Redis 캐시에 값 추가
      chatHistoryCacheService.addMessage(teamId, ChatMessageBroadcastDto.of(chatMessage));

      // 반환하기 위해 result에 추가
      result.add(ChatMessageBroadcastDto.of(chatMessage));
    }

    log.info("Redis에 캐싱되어있지 않아 저장하고 결과 반환");

    // 결과 반환x
    return result;
  }

  @Transactional(readOnly = true)
  public List<ChatMessageBroadcastDto> getAllMessageHistory(Long memberId, Long teamId) {
    // 여기서 이 유저가 채팅방에 접근할 권한 있는지 체크
    this.authCheck(memberId, teamId);

    // 저장할 배열 생성
    List<ChatMessageBroadcastDto> result = new ArrayList<>();

    // Mongo에서 값 불러오기
    Query q = new Query();
    q.addCriteria(Criteria.where("teamId").is(teamId));
    List<ChatMessage> result_mongo = mongoTemplate.find(q, ChatMessage.class);

    // 각 값을 ChatMessage -> ChatMessageBroadCastDto로 변경
    for (ChatMessage chatMessage : result_mongo) {
      // Redis 캐시에 값 추가
      chatHistoryCacheService.addMessage(teamId, ChatMessageBroadcastDto.of(chatMessage));

      // 반환하기 위해 result에 추가
      result.add(ChatMessageBroadcastDto.of(chatMessage));
      log.info("{}가 result에 더해짐", chatMessage);
    }

    // 결과 반환
    return result;
  }

  // 특정 채팅방에 유저가 접근할 권한이 있는지 확인한다.
  private void authCheck(Long memberId, Long teamId) {
    if (!teamMemberStatusRepository.existsByTeam_TeamIdAndMember_MemberIdAndStatus(teamId, memberId,
        ParticipationStatus.APPROVED)) {
      log.info("{} 멤버는 {}번 그룹의 멤버가 아닙니다.", memberId, teamId);
      throw BaseException.ACCESS_DENIED;
    }
  }

  public void deleteMessagesByTeamId(Long teamId) {
    Query query = new Query(Criteria.where("teamId").is(teamId));
    mongoTemplate.remove(query, ChatMessage.class);
  }
}

