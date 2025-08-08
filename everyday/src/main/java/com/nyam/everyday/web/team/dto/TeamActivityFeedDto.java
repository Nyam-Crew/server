package com.nyam.everyday.web.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 그룹 실시간 현황 DTO
 *
 * @author : 이지은
 * @fileName : TeamActivityFeedDTO
 * @since : 25. 8. 4.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamActivityFeedDto{

    @Schema(description = "피드 ID", example = "")
    private Long id;

    @Schema(description = "그룹 ID", example = "ssj@naver.com")
    private Long teamId;

    @Schema(description = "그룹 멤버 ID", example = "ssj@naver.com")
    private Long memberId;

    @Schema(description = "활동 타입", example = "ssj@naver.com")
    private String activityType;

    @Schema(description = "활동 내용", example = "ssj@naver.com")
    private String activityContent;

    @Schema(description = "피드 생성 시간", example = "ssj@naver.com")
    private LocalDateTime createdAt;
}