package com.nyam.everyday.module.team.event;

import lombok.Getter;

/**
 * 비동기 처리를 위한 이벤트 객체
 *
 * @author : 이지은
 * @fileName : TeamFeedCreatedEvent
 * @since : 25. 8. 22.
 */
@Getter
public class TeamFeedCreatedEvent {

    private final Long actorMemberId;
    private final Long teamId;
    private final String content;

    public TeamFeedCreatedEvent(Long actorMemberId, Long teamId, String content) {
        this.actorMemberId = actorMemberId;
        this.teamId = teamId;
        this.content = content;
    }
}