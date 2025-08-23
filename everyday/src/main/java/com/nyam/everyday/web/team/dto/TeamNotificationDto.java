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

    @Schema(name = "알림ID", example = "1")
    private Long teamAlarmId;

    @Schema(name = "알림 발생 그룹", example = "1")
    private Long teamId;

    @Schema(name = "알림 전송 대상", example = "1")
    private Long memberId;

    @Schema(name = "전송 알림 타입", example = "NOTICE, FEED, CHAT")
    private TeamNotificationType notificationType;

    @Schema(name= "그룹 알림 내용", example = "새로운 공지가 발생되었습니다.")
    private String teamNotyContent;

    @Schema(name="그룹 알림 확인", example="false")
    private boolean isChecked = false;

    @Schema(name = "알림생성날짜")
    private LocalDateTime teamAlarmCreatedDate;

    @Schema(name="그룹명", example="운동하는 사자들")
    private String teamTitle;
}