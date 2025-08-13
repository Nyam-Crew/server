package com.nyam.everyday.module.team.service;

import com.nyam.everyday.web.team.dto.TeamActivityFeedItem;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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
    void record(Long teamId, Long actorMemberId, String type,
                String title, String message, Map<String, Object> payload);

    /** 최근 N개 */
    List<TeamActivityFeedItem> listRecent(Long teamId, Long requesterMemberId, int size);

    /** 커서 이전 조회(무한 스크롤) */
    List<TeamActivityFeedItem> listBefore(Long teamId, Long requesterMemberId,
                                          Instant cursor, int size);
}