package com.nyam.everyday.module.team.service;

import com.nyam.everyday.web.team.dto.FeedSlice;
import com.nyam.everyday.web.team.dto.TeamActivityFeedItem;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * Redis 실시간 피드 인터페이스
 *
 * @author : 이지은
 * @fileName : TeamActivityFeedService
 * @since : 25. 8. 13.
 *
 */
/** 팀 실시간 피드 서비스 (Redis MVP) */
public interface TeamActivityFeedService {

    /**
     * 실시간 피드 기록
     * @param teamId        팀 ID
     * @param actorMemberId 행위자(작성자) ID
     * @param type          활동 타입(enum 이름 문자열; ActivityType.valueOf에 사용)
     * @param title         제목(요약) - 프론트 렌더링용
     * @param message       본문(상세) - 프론트 렌더링용
     * @param payload       추가 데이터(선택; MVP에서는 저장/송신 생략 가능)
     */
    /** 최초 생성(정렬 score=createdAtMs 고정) */
    void addFeedItemToTeams(Set<Long> teamIds,
                     String feedId,
                     long createdAtMs,
                     TeamActivityFeedItem item,
                     Duration ttl);

    /** 내용만 갱신(순서/score 유지, TTL 유지) */
    Map<Long, Boolean> updateFeedItemValueInTeams(Set<Long> teamIds,
                                                  String feedId,
                                                  TeamActivityFeedItem updated);

    /** 팀피드 삭제 */
    void removeFeedItem(Set<Long> teamIds, String feedId);

    /** 오프셋 기반(기존 유지): 최신순, start/size */
    List<TeamActivityFeedItem> listFeed(Long teamId, int start, int size);

    /** 커서 기반 무한스크롤: cursor(미포함) 이전 항목을 최신순으로 size개 */
    FeedSlice listFeedBefore(Long teamId, Long cursorEpochMs, int size, Long currentMemberId);

    /** (선택) 위로 당겨서 새로고침: cursor(미포함) 이후 ‘신규’ 항목들을 최신순으로 */
    FeedSlice listFeedAfter(Long teamId, Long cursorEpochMs, int size);
}