package com.nyam.everyday.redis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyam.everyday.web.chatting.dto.ChatMessageBroadcastDto;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


/**
 * 채팅방별로 "최근 N개 메시지"를 Redis에 보관하는 캐시 서비스
 *
 * - 자료구조: Redis List
 *   - 오른쪽으로 추가(RPUSH), 오래된 앞부분을 잘라내는(LTRIM) 방식으로
 *     항상 최대 길이(CHAT_MAX_SIZE)만 유지
 *
 * - StringRedisTemplate을 쓰는 이유
 *   - Redis의 key와 value 직렬화가 기본 문자열(String)로 고정되어 있음
 *   - DTO는 ObjectMapper로 JSON 문자열로 바꿔 저장
 *   - 읽을 때는 JSON 문자열을 DTO로 역직렬화
 *   → 언어 간 호환이 쉽고 키/값을 Redis CLI로 직접 확인하기도 편리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatHistoryCacheService {

  /** 문자열 기반 Redis 접근 헬퍼
   *  - opsForXxx() 들은 Redis의 자료구조별(값 타입별) 연산 헬퍼를 반환
   *    opsForValue()  : String
   *    opsForHash()   : Hash
   *    opsForList()   : List   ← 여기서 사용
   *    opsForSet()    : Set
   *    opsForZSet()   : Sorted Set
   */
  private final StringRedisTemplate redisConnector;

  /** DTO <-> JSON 변환기 */
  private final ObjectMapper objectMapper;

  /** 방별 메시지 리스트가 들어갈 키 포맷 */
  private static final String CHAT_MESSAGE_KEY_FMT = "chat:room:%d:messages";

  /** 리스트 최대 보관 개수 */
  private static final int CHAT_MAX_SIZE = 20;

  /** 선택: 방이 한동안 사용되지 않으면 캐시 자동 정리하고 싶을 때 TTL 사용 */
  private static final long CHAT_TTL_MINUTES = 30;

  /**
   * 메시지 1건 추가
   * - RPUSH로 리스트 오른쪽에 추가
   * - LTRIM으로 최근 CHAT_MAX_SIZE개만 남기고 앞부분 삭제
   * - 선택: TTL이 없으면 TTL 부여
   */
  public void addMessage(Long chatRoomId, ChatMessageBroadcastDto chatMessageBroadcastDto) {
    String key = CHAT_MESSAGE_KEY_FMT.formatted(chatRoomId);

    try {
      // DTO -> JSON 문자열
      String json = objectMapper.writeValueAsString(chatMessageBroadcastDto);

      // opsForList()는 "List 자료구조 전용 연산자"를 반환
      // rightPush = RPUSH
      redisConnector.opsForList().rightPush(key, json);

      // trim(start, end) → LTRIM
      // -CHAT_MAX_SIZE ~ -1 범위만 남겨 "최근 N개 유지"
      redisConnector.opsForList().trim(key, -CHAT_MAX_SIZE, -1);

      // TTL 갱신
      redisConnector.expire(key, CHAT_TTL_MINUTES, TimeUnit.MINUTES);

    } catch (JsonProcessingException e) {
      log.error("[ChatHistoryCacheService] JSON 직렬화 실패: {}", e.getMessage(), e);
    }
  }

  /**
   * 방의 메시지 전체(또는 필요 시 부분 범위) 조회
   * - range(0, -1) = 리스트 전체 반환(LRANGE 0 -1)
   * - 꺼낸 JSON 문자열을 DTO로 역직렬화하여 반환
   *
   * "String으로 매핑했다가 푸는" 이유와 방법:
   *  1) 저장 시: DTO → JSON(String)으로 변환해 Redis List에 저장
   *  2) 조회 시: JSON(String) → DTO로 역직렬화(ObjectMapper.readValue)
   *  → 문자열로 저장하면 디버깅이 쉽고, 언어 간 호환도 좋음
   */
  public List<ChatMessageBroadcastDto> getMessages(Long chatRoomId) {
    String key = CHAT_MESSAGE_KEY_FMT.formatted(chatRoomId);

    // 전체 구간: 0부터 끝(-1)까지
    List<String> raw = redisConnector.opsForList().range(key, 0, -1);
    if (raw == null || raw.isEmpty()) {
      return List.of();
    }

    List<ChatMessageBroadcastDto> result = new ArrayList<>(raw.size());
    for (String s : raw) {
      try {
        // JSON(String) -> DTO
        ChatMessageBroadcastDto dto = objectMapper.readValue(s, ChatMessageBroadcastDto.class);
        result.add(dto);
      } catch (Exception e) {
        // 한 건 파싱 실패해도 나머지는 살려서 반환
        log.warn("[ChatHistoryCacheService] JSON 역직렬화 실패. 건너뜀. value={}", s, e);
      }
    }
    return result;
  }

  /* 필요 시 확장용 메소드 예시
  public List<ChatMessageBroadcastDto> getRecent(Long chatRoomId, int limit) {
    String key = CHAT_MESSAGE_KEY_FMT.formatted(chatRoomId);
    // 끝에서 limit개: LRANGE -limit -1
    List<String> raw = redisConnector.opsForList().range(key, -limit, -1);
    if (raw == null || raw.isEmpty()) return List.of();

    List<ChatMessageBroadcastDto> result = new ArrayList<>(raw.size());
    for (String s : raw) {
      try {
        result.add(objectMapper.readValue(s, ChatMessageBroadcastDto.class));
      } catch (Exception ignored) {}
    }
    return result;
  }
  */
}



