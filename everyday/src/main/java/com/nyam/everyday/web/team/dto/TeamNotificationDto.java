package com.nyam.everyday.web.team.dto;

import com.nyam.everyday.module.team.enums.TeamNotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 그룹 알림 DTO
 *
 * @author : 이지은
 * @fileName : TeamNotificationDTO
 * @since : 25. 8. 4.
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamNotificationDto {

    @Schema(name = "", example = "")
    private Long teamAlarmId;

    @Schema(name = "", example = "")
    private Long teamId;

    @Schema(name = "", example = "")
    private Long memberId;

    @Schema(name = "", example = "")
    private TeamNotificationType notificationType;

    @Schema(name = "", example = "")
    private LocalDateTime teamAlarmCreatedDate;
}