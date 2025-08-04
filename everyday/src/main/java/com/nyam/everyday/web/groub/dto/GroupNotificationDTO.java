package com.nyam.everyday.web.groub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 그룹 알림 DTO
 *
 * @author : 이지은
 * @fileName : GroupNotificationDTO
 * @since : 25. 8. 4.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupNotificationDTO {

    private Long id;
    private Long groupId;
    private Long memberId;
    private String notificationType;
    private LocalDateTime createdDate;
}