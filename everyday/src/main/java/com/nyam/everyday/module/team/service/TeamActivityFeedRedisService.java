package com.nyam.everyday.module.team.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyam.everyday.module.team.view.TeamFeedMessageFormatter;
import com.nyam.everyday.web.team.dto.FeedSlice;
import com.nyam.everyday.web.team.dto.TeamActivityFeedItem;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
public class TeamActivityFeedRedisService implements TeamActivityFeedService {

    private final RedisTemplate<String, String> redisTemplate; // value는 JSON 문자열 저장
    private final ObjectMapper objectMapper;

    public TeamActivityFeedRedisService(@Qualifier("redisTeamFeedTemplate") RedisTemplate<String, String> redisTemplate,
        ObjectMapper objectMapper ){
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    private String indexKey(Long teamId) { return "team:%d:activity:index".formatted(teamId); }
    private String itemKey(Long teamId, String feedId) { return "team:%d:activity:item:%s".formatted(teamId, feedId); }

    /* ===================== 생성(복수형) ===================== */

    /** 최초 생성(여러 팀). 이미 존재하면 score/TTL은 유지하고 값만 덮음(남은 TTL 유지). */
    @Override
    public void addFeedItemToTeams(Set<Long> teamIds,
                                   String feedId,
                                   long createdAtMs,
                                   TeamActivityFeedItem item,
                                   Duration ttl) {
        if (teamIds == null || teamIds.isEmpty()) return;

        // 메시지 준비(팀별 teamId만 주입해서 사용)
        ensureCreatedAt(item, createdAtMs);

        for (Long teamId : teamIds) {
            TeamActivityFeedItem perTeam = copyForTeam(item, teamId);
            perTeam.setActivityMessage(TeamFeedMessageFormatter.formatLine(perTeam));

            final String idxKey = indexKey(teamId);
            final String itKey  = itemKey(teamId, feedId);

            // 1) ZSET: 최초에만 score 세팅(순서 고정)
            Double existingScore = redisTemplate.opsForZSet().score(idxKey, feedId);
            if (existingScore == null) {
                redisTemplate.opsForZSet().add(idxKey, feedId, createdAtMs);
            }

            // 2) KV 저장: 존재하면 값만 갱신 + 남은 TTL 유지, 없으면 TTL 부여
            Long remainMs = redisTemplate.getExpire(itKey, TimeUnit.MILLISECONDS);
            String json = toJson(perTeam);
            if (remainMs == null || remainMs <= 0) {
                redisTemplate.opsForValue().set(itKey, json, ttl);
            } else {
                redisTemplate.opsForValue().set(itKey, json);
                redisTemplate.expire(itKey, remainMs, TimeUnit.MILLISECONDS);
            }
        }



    }

    /* ===================== 갱신(복수형, 값만) ===================== */

    /**
     * 내용만 갱신(여러 팀). 순서(score)·TTL 유지.
     * 존재하지 않거나 TTL 만료된 팀은 false 반환(재생성하지 않음).
     */
    @Override
    public Map<Long, Boolean> updateFeedItemValueInTeams(Set<Long> teamIds,
                                                         String feedId,
                                                         TeamActivityFeedItem updated) {
        Map<Long, Boolean> result = new LinkedHashMap<>();
        if (teamIds == null || teamIds.isEmpty()) return result;

        for (Long teamId : teamIds) {
            final String itKey = itemKey(teamId, feedId);

            // 0) 남은 TTL (없으면 만료/삭제됨 → 갱신 생략)
            Long remainMs = redisTemplate.getExpire(itKey, TimeUnit.MILLISECONDS);
            if (remainMs == null || remainMs <= 0) {
                result.put(teamId, false);
                continue;
            }

            // 1) 기존 로드
            String originJson = redisTemplate.opsForValue().get(itKey);
            if (originJson == null) {
                result.put(teamId, false);
                continue;
            }

            TeamActivityFeedItem origin = fromJson(originJson);

            // 2) 정렬/식별 필드 유지
            if (updated.getFeedId() == null) updated.setFeedId(origin.getFeedId());
            if (updated.getTeamId() == null) updated.setTeamId(origin.getTeamId());
            if (updated.getMemberId() == null) updated.setMemberId(origin.getMemberId());
            if (updated.getNickname() == null) updated.setNickname(origin.getNickname());
            if (updated.getProfileImageUrl() == null) updated.setProfileImageUrl(origin.getProfileImageUrl());
            if (updated.getActivityType() == null) updated.setActivityType(origin.getActivityType());
            if (updated.getFeedCreatedDate() == null) updated.setFeedCreatedDate(origin.getFeedCreatedDate());

            // 3) 메시지 재생성
            updated.setActivityMessage(TeamFeedMessageFormatter.formatLine(updated));

            // 4) 값만 교체 + 남은 TTL 유지
            redisTemplate.opsForValue().set(itKey, toJson(updated));
            redisTemplate.expire(itKey, remainMs, TimeUnit.MILLISECONDS);

            result.put(teamId, true);
        }
        return result;
    }

    /* ===================== 삭제(복수형) ===================== */

    /** 단건 삭제(여러 팀). KV 삭제 + 인덱스 멤버 제거(멱등). */
    @Override
    public void removeFeedItem(Set<Long> teamIds, String feedId) {
        if (teamIds == null || teamIds.isEmpty()) return;

        for (Long teamId : teamIds) {
            redisTemplate.delete(itemKey(teamId, feedId));
            redisTemplate.opsForZSet().remove(indexKey(teamId), feedId);
        }
    }

    /* ===================== 조회(팀 단위) ===================== */

    /** 피드 조회(최신순, start/size 기반) */
    @Override
    public List<TeamActivityFeedItem> listFeed(Long teamId, int start, int size) {
        String idxKey = indexKey(teamId);
        long end = start + size - 1;
        Set<String> feedIds = castStringSet(redisTemplate.opsForZSet().reverseRange(idxKey, start, end));
        if (feedIds == null || feedIds.isEmpty()) return List.of();

        List<String> keys = feedIds.stream().map(fid -> itemKey(teamId, fid)).collect(Collectors.toList());
        List<String> jsons = redisTemplate.opsForValue().multiGet(keys);

        List<TeamActivityFeedItem> out = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        int i = 0;
        for (String fid : feedIds) {
            String json = (jsons != null && i < jsons.size()) ? jsons.get(i) : null;
            if (json == null) {
                missing.add(fid); // TTL 만료 등 → 게으른 정리
            } else {
                TeamActivityFeedItem item = fromJson(json);
                if (item.getActivityMessage() == null || item.getActivityMessage().isBlank()) {
                    item.setActivityMessage(TeamFeedMessageFormatter.formatLine(item));
                }
                out.add(item);
            }
            i++;
        }
        if (!missing.isEmpty()) {
            redisTemplate.opsForZSet().remove(idxKey, missing.toArray());
        }
        return out;
    }

    @Override
    public FeedSlice listFeedBefore(Long teamId, Long cursorEpochMs, int size) {
        String idxKey = indexKey(teamId);

        double max = (cursorEpochMs == null) ? Double.POSITIVE_INFINITY : (double) (cursorEpochMs - 1L);
        double min = 0d;

        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeByScoreWithScores(idxKey, min, max, 0, size);

        if (tuples == null || tuples.isEmpty()) {
            return FeedSlice.builder().items(Collections.emptyList()).nextCursorEpochMs(null).hasNext(false).build();
        }

        List<String> feedIds = tuples.stream().map(ZSetOperations.TypedTuple::getValue).collect(Collectors.toList());
        List<String> keys = feedIds.stream().map(fid -> itemKey(teamId, fid)).collect(Collectors.toList());
        List<String> jsons = redisTemplate.opsForValue().multiGet(keys);

        List<TeamActivityFeedItem> items = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        List<Long> scoresKept = new ArrayList<>();

        int i = 0;
        for (ZSetOperations.TypedTuple<String> t : tuples) {
            String fid = t.getValue();
            Double sc = t.getScore();
            String json = (jsons != null && i < jsons.size()) ? jsons.get(i) : null;

            if (json == null) {
                missing.add(fid);
            } else {
                TeamActivityFeedItem item = fromJson(json);
                if (item.getActivityMessage() == null || item.getActivityMessage().isBlank()) {
                    item.setActivityMessage(TeamFeedMessageFormatter.formatLine(item));
                }
                items.add(item);
                scoresKept.add(sc == null ? null : sc.longValue());
            }
            i++;
        }
        if (!missing.isEmpty()) redisTemplate.opsForZSet().remove(idxKey, missing.toArray());

        Long nextCursor = items.isEmpty() ? null : scoresKept.get(scoresKept.size() - 1);

        boolean hasMore = false;
        if (nextCursor != null) {
            Long more = redisTemplate.opsForZSet().count(idxKey, 0d, (double) (nextCursor - 1));
            hasMore = more != null && more > 0;
        }

        return FeedSlice.builder().items(items).nextCursorEpochMs(nextCursor).hasNext(hasMore).build();
    }

    @Override
    public FeedSlice listFeedAfter(Long teamId, Long cursorEpochMs, int size) {
        String idxKey = indexKey(teamId);

        if (cursorEpochMs == null) {
            List<TeamActivityFeedItem> latest = listFeed(teamId, 0, size);
            Long nextCursor = latest.isEmpty() ? null : getMinCreatedAtMs(latest);
            return FeedSlice.builder().items(latest).nextCursorEpochMs(nextCursor).hasNext(!latest.isEmpty()).build();
        }

        double min = (double) (cursorEpochMs + 1L);
        double max = Double.POSITIVE_INFINITY;

        Set<ZSetOperations.TypedTuple<String>> asc =
                redisTemplate.opsForZSet().rangeByScoreWithScores(idxKey, min, max, 0, size);

        if (asc == null || asc.isEmpty()) {
            return FeedSlice.builder().items(Collections.emptyList()).nextCursorEpochMs(null).hasNext(false).build();
        }

        List<ZSetOperations.TypedTuple<String>> tuples = new ArrayList<>(asc);
        Collections.reverse(tuples); // 최신순으로 전환

        List<String> feedIds = tuples.stream().map(ZSetOperations.TypedTuple::getValue).collect(Collectors.toList());
        List<String> keys = feedIds.stream().map(fid -> itemKey(teamId, fid)).collect(Collectors.toList());
        List<String> jsons = redisTemplate.opsForValue().multiGet(keys);

        List<TeamActivityFeedItem> items = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        int i = 0;
        for (ZSetOperations.TypedTuple<String> t : tuples) {
            String fid = t.getValue();
            String json = (jsons != null && i < jsons.size()) ? jsons.get(i) : null;

            if (json == null) {
                missing.add(fid);
            } else {
                TeamActivityFeedItem item = fromJson(json);
                if (item.getActivityMessage() == null || item.getActivityMessage().isBlank()) {
                    item.setActivityMessage(TeamFeedMessageFormatter.formatLine(item));
                }
                items.add(item);
            }
            i++;
        }
        if (!missing.isEmpty()) redisTemplate.opsForZSet().remove(idxKey, missing.toArray());

        Long nextCursor = items.isEmpty() ? null : getMinCreatedAtMs(items);
        boolean hasMore = items.size() == size;

        return FeedSlice.builder().items(items).nextCursorEpochMs(nextCursor).hasNext(hasMore).build();
    }

    /* ===================== 유틸 ===================== */

    private void ensureCreatedAt(TeamActivityFeedItem item, long createdAtMs) {
        if (item.getFeedCreatedDate() == null) {
            item.setFeedCreatedDate(Instant.ofEpochMilli(createdAtMs).atZone(ZoneOffset.UTC).toLocalDateTime());
        }
    }

    private TeamActivityFeedItem copyForTeam(TeamActivityFeedItem src, Long teamId) {
        return TeamActivityFeedItem.builder()
                .feedId(src.getFeedId())
                .teamId(teamId)
                .memberId(src.getMemberId())
                .nickname(src.getNickname())
                .profileImageUrl(src.getProfileImageUrl())
                .activityType(src.getActivityType())
                .activityMessage(src.getActivityMessage())
                .feedCreatedDate(src.getFeedCreatedDate())
                .amountMl(src.getAmountMl())
                .mealPeriod(src.getMealPeriod())
                .kcal(src.getKcal())
                //.menu(src.getMenu())
                .thumbnailUrl(src.getThumbnailUrl())
                .weightKg(src.getWeightKg())
                .deltaKg(src.getDeltaKg())
                .challengeName(src.getChallengeName())
                .build();
    }

    private String toJson(TeamActivityFeedItem item) {
        try { return objectMapper.writeValueAsString(item); }
        catch (Exception e) { throw new RuntimeException("Feed JSON serialize error", e); }
    }

    private TeamActivityFeedItem fromJson(String json) {
        try { return objectMapper.readValue(json, TeamActivityFeedItem.class); }
        catch (Exception e) { throw new RuntimeException("Feed JSON deserialize error", e); }
    }

    @SuppressWarnings("unchecked")
    private static Set<String> castStringSet(Set<?> s) {
        if (s == null) return null;
        Set<String> out = new LinkedHashSet<>(s.size());
        for (Object o : s) out.add(String.valueOf(o));
        return out;
    }

    private static Long getMinCreatedAtMs(List<TeamActivityFeedItem> items) {
        return items.stream()
                .map(TeamActivityFeedItem::getFeedCreatedDate)
                .filter(Objects::nonNull)
                .map(dt -> dt.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
                .min(Long::compareTo)
                .orElse(null);
    }
}