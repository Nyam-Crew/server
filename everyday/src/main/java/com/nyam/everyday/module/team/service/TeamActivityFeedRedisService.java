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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    private static final long TTL_WINDOW_MS = 24L * 60 * 60 * 1000;      // 24시간
    private static final long MAX_FEED_ITEMS_PER_TEAM = 5000;            // 팀별 상한
    private static final String TEAMS_WITH_FEED_KEY = "teams:withFeed";  // 스케줄 청소용

    private String zsetKey(Long teamId) { return "team:" + teamId + ":feed"; }

    @Override
    public void record(Long teamId, Long actorMemberId, String type,
                       String title, String message, Map<String, Object> payload) {

        // 1) 권한 체크: APPROVED 멤버만
        TeamMemberStatus rel = teamMemberStatusRepository
                .findByTeam_TeamIdAndMember_MemberId(teamId, actorMemberId)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCESS_DENIED));
        if (rel.getStatus() != ParticipationStatus.APPROVED) {
            throw new BaseException(ErrorCode.ACCESS_DENIED);
        }

        // 2) ActivityType 파싱
        final ActivityType activityType;
        try {
            activityType = ActivityType.valueOf(type);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BaseException(ErrorCode.INVALID_REQUEST, "잘못된 activity type: " + type);
        }

        // 3) 표시용 content 선택(간단 규칙: title 우선, 없으면 message)
        final String activityContent = (title != null && !title.isBlank()) ? title : message;

        // 4) DTO 생성 (Mapper.create 사용) — feedId/createdDate는 매퍼에서 자동 세팅
        TeamActivityFeedItem item = feedMapper.create(
                teamId,
                actorMemberId,
                activityType,
                activityContent,
                fileNameGenerator // @Context
        );

        // 5) Redis ZSET 저장 (score = feedCreatedDate epochMillis)
        String key = zsetKey(teamId);
        long epochMillis = toEpochMillis(item.getFeedCreatedDate());

        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
        zset.add(key, item, epochMillis);

        // TTL 정리(24h 초과 제거)
        long threshold = epochMillis - TTL_WINDOW_MS;
        zset.removeRangeByScore(key, 0, threshold);

        // 팀별 상한 초과 시 오래된 것 제거
        Long size = zset.zCard(key);
        if (size != null && size > MAX_FEED_ITEMS_PER_TEAM) {
            zset.removeRange(key, 0, size - MAX_FEED_ITEMS_PER_TEAM - 1);
        }

        // 스케줄 청소 대상 등록
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        setOps.add(TEAMS_WITH_FEED_KEY, teamId);

        // 6) 실시간 브로드캐스트
        messagingTemplate.convertAndSend("/topic/teams/" + teamId + "/feed", item);

        // (MVP에선 payload는 저장/송신 생략. 필요시 DTO에 필드 추가해서 함께 저장/전송)
    }

    @Override
    public List<TeamActivityFeedItem> listRecent(Long teamId, Long requesterMemberId, int size) {
        verifyApprovedMember(teamId, requesterMemberId);
        Set<Object> raw = redisTemplate.opsForZSet()
                .reverseRange(zsetKey(teamId), 0, Math.max(0, size - 1));
        return cast(raw);
    }

    @Override
    public List<TeamActivityFeedItem> listBefore(Long teamId, Long requesterMemberId, Instant cursor, int size) {
        verifyApprovedMember(teamId, requesterMemberId);
        double max = (cursor == null ? Double.POSITIVE_INFINITY : cursor.toEpochMilli() - 1);
        Set<Object> raw = redisTemplate.opsForZSet()
                .reverseRangeByScore(zsetKey(teamId), max, 0, 0, size);
        return cast(raw);
    }

    private void verifyApprovedMember(Long teamId, Long memberId) {
        teamMemberStatusRepository.findByTeam_TeamIdAndMember_MemberId(teamId, memberId)
                .filter(rel -> rel.getStatus() == ParticipationStatus.APPROVED)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCESS_DENIED));
    }

    @SuppressWarnings("unchecked")
    private List<TeamActivityFeedItem> cast(Set<Object> raw) {
        if (raw == null || raw.isEmpty()) return List.of();
        return raw.stream().filter(Objects::nonNull)
                .map(o -> (TeamActivityFeedItem) o)
                .collect(Collectors.toList());
    }

    private long toEpochMillis(LocalDateTime ldt) {
        // 서버 표준시 고정(권장: UTC) – 프론트에서 타임존 렌더링
        return ldt.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    /** 안전망: 60초마다 24h 경과 데이터 일괄 제거 */
    @Scheduled(fixedDelay = 60_000)
    public void scheduledCleanup() {
        long threshold = System.currentTimeMillis() - TTL_WINDOW_MS;
        Set<Object> teamIds = redisTemplate.opsForSet().members(TEAMS_WITH_FEED_KEY);
        if (teamIds == null || teamIds.isEmpty()) return;

        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
        for (Object tid : teamIds) {
            Long teamId = (tid instanceof Number) ? ((Number) tid).longValue() : Long.valueOf(tid.toString());
            zset.removeRangeByScore(zsetKey(teamId), 0, threshold);
        }
    }
}