package com.nyam.everyday.web.team.dto;

import com.nyam.everyday.module.team.enums.TeamNotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 *
 * 알림함 갱신을 위한 Dto
 *
 * @author : 이지은
 * @fileName : TeamNotificationBoxDto
 * @since : 25. 8. 21.
 *
 */
@Getter
@Builder
public class TeamNotificationBoxDto {
    private Long notificationId;
    private Long teamId;
    private String content;                 // teamNotyContent
    private TeamNotificationType type;      // CHAT/FEED/NOTICE/SUMMARY
    private LocalDateTime createdAt;
    private boolean isRead;
}