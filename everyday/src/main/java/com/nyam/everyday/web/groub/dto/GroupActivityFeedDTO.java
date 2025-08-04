package com.nyam.everyday.web.groub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 그룹 실시간 현황 DTO
 *
 * @author : 이지은
 * @fileName : GroupActivityFeedDTO
 * @since : 25. 8. 4.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupActivityFeedDTO {

    private Long id;
    private Long groupId;
    private Long memberId;
    private String activityType;
    private String activityContent;
    private LocalDateTime createdAt;
}