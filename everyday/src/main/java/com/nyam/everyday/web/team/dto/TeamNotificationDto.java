package com.nyam.everyday.web.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 그룹 알림 DTO
 *
 * @author : 이지은
 * @fileName : TeamNotificationDTO
 * @since : 25. 8. 4.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamNotificationDTO {

    @Schema(description = "사용자 이메일", example = "ssj@naver.com")
    private Long id;
    private Long teamId;
    private Long memberId;
    private String notificationType;
    private LocalDateTime createdDate;
}