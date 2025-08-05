package com.nyam.everyday.web.groub.dto;

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

    @Schema(name = "", example = "")
    private Long teamAlarmId;

    @Schema(name = "", example = "")
    private Long teamId;

    @Schema(name = "", example = "")
    private Long memberId;

    @Schema(name = "", example = "")
    private String notificationType;

    @Schema(name = "", example = "")
    private LocalDateTime teamAlarmCreatedDate;
}