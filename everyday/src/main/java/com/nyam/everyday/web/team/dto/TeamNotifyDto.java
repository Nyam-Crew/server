package com.nyam.everyday.web.team.dto;

import com.nyam.everyday.module.team.enums.TeamNotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 *
 * 일반 알림 DTO와 분리
 *
 * @author : 이지은
 * @fileName : TeamNotifyDto
 * @since : 25. 8. 20.
 *
 */
@Getter
@Builder
public class TeamNotifyDto {
    //기존 필드
    private String content;
    private LocalDateTime createdAt;
    //확장 필드
    private Long notificationId;       // 알림 고유 ID (어떤 알림인지 식별)
    private TeamNotificationType type; // 알림 종류
    //private String linkUrl;            // 클릭 시 이동할 URL
}