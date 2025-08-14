package com.nyam.everyday.module.team.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.common.util.FileNameGenerator;
import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.module.team.enums.ActivityType;
import com.nyam.everyday.module.team.enums.ParticipationStatus;
import com.nyam.everyday.module.team.repository.TeamMemberStatusRepository;
import com.nyam.everyday.web.team.dto.TeamActivityFeedItem;
import com.nyam.everyday.web.team.mapper.TeamActivityFeedItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * 활동피드 서비스
 *
 * @author : 이지은
 * @fileName : TeamActivityFeedService
 * @since : 25. 8. 13.
 *
 */
@Service
@RequiredArgsConstructor
public class TeamActivityFeedRedisService implements TeamActivityFeedService {

    private final TeamMemberStatusRepository teamMemberStatusRepository;
    private final RedisTemplate<String, Object> redisTemplate;           // value: GenericJackson2JsonRedisSerializer
    private final SimpMessagingTemplate messagingTemplate;
    private final TeamActivityFeedItemMapper feedMapper;                 // ✅ 네가 준 Mapper 시그니처
    private final FileNameGenerator fileNameGenerator;                   // @Context로 주입

    // 24시간 만료 (아이템 키에 TTL)
    private static final Duration TTL_24H = Duration.ofHours(24);
    // ZSET 인덱스에서 가져올 때, 존재하지 않는(만료된) 아이템은 스캔 중 정리
    private static final int BATCH_FETCH = 100;  // 스케줄 청소용

    private String itemKey(String feedId) {
        return "team:feed:item:" + feedId;
    }

    private String indexKey(Long teamId) {
        return "team:" + teamId + ":feed:index";
    }

    public void record(Long teamId, Long actorMemberId,
                       ActivityType activityType,
                       String title, String message, Map<String, Object> payload) {

        // 1) 권한: APPROVED 멤버만
        verifyApproved(teamId, actorMemberId);

        // 2) 보여줄 메시지(간단 규칙: title 우선, 없으면 message)
        String activityContent = (title != null && !title.isBlank()) ? title : message;

        // 3) DTO 생성 (ID/생성시각은 매퍼에서 자동 세팅)
        TeamActivityFeedItem item = feedMapper.create(
                teamId, actorMemberId, activityType, activityContent, fileNameGenerator);

        // 4) 아이템 저장 + TTL(24h) 예약
        String itemK = itemKey(item.getFeedId());
        redisTemplate.opsForValue().set(itemK, item, TTL_24H);

        // 5) 팀별 인덱스(ZSET)에 feedId 추가 (score=createdAt epochMillis)
        String indexK = indexKey(teamId);
        long score = toEpochMillis(item.getFeedCreatedDate());
        redisTemplate.opsForZSet().add(indexK, item.getFeedId(), score);

        // (선택) 인덱스에서 24h 이전 score 제거 (쓰기 시 정리)
        long cutoff = System.currentTimeMillis() - TTL_24H.toMillis();
        redisTemplate.opsForZSet().removeRangeByScore(indexK, 0, cutoff);

        // 6) 실시간 브로드캐스트
        messagingTemplate.convertAndSend("/topic/teams/" + teamId + "/feed", item);
    }

    @Override
    public List<TeamActivityFeedItem> listRecent(Long teamId, Long requesterMemberId, int size) {
        verifyApproved(teamId, requesterMemberId);
        String kIndex = indexKey(teamId);

        // 최신 feedId들 조회
        Set<Object> ids = redisTemplate.opsForZSet().reverseRange(kIndex, 0, Math.max(0, size - 1));
        return loadExistingItemsAndCleanupMissing(kIndex, ids);
    }

    @Override
    public List<TeamActivityFeedItem> listBefore(Long teamId, Long requesterMemberId, Instant cursor, int size) {
        verifyApproved(teamId, requesterMemberId);
        String kIndex = indexKey(teamId);

        double max = (cursor == null ? Double.POSITIVE_INFINITY : cursor.toEpochMilli() - 1);
        Set<Object> ids = redisTemplate.opsForZSet()
                .reverseRangeByScore(kIndex, max, 0, 0, size);

        return loadExistingItemsAndCleanupMissing(kIndex, ids);
    }

    /* ===== 내부 유틸 ===== */

    private void verifyApproved(Long teamId, Long memberId) {
        teamMemberStatusRepository.findByTeam_TeamIdAndMember_MemberId(teamId, memberId)
                .filter(rel -> rel.getStatus() == ParticipationStatus.APPROVED)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCESS_DENIED));
    }

    private List<TeamActivityFeedItem> loadExistingItemsAndCleanupMissing(String indexKey, Set<Object> ids) {
        if (ids == null || ids.isEmpty()) return List.of();

        List<TeamActivityFeedItem> result = new ArrayList<>(ids.size());
        List<String> toRemove = new ArrayList<>();

        for (Object idObj : ids) {
            String feedId = String.valueOf(idObj);
            Object val = redisTemplate.opsForValue().get(itemKey(feedId));
            if (val == null) {
                // 아이템은 TTL로 만료됨 → 인덱스에서 제거 (게으른 정리)
                toRemove.add(feedId);
            } else {
                result.add((TeamActivityFeedItem) val);
            }
        }
        if (!toRemove.isEmpty()) {
            // 현재 시점에서 missing id 정리
            redisTemplate.opsForZSet().remove(indexKey, toRemove.toArray());
        }
        // DTO는 최신순으로 읽혔으므로 추가 정렬 불필요
        return result;
    }

    private long toEpochMillis(LocalDateTime ldt) {
        // 서버 표준시(UTC) 기준으로 처리; 프론트에서 타임존 렌더링
        return ldt.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    /* (참고) 디버깅용 */
    private String fmt(LocalDateTime ldt) {
        return ldt.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}