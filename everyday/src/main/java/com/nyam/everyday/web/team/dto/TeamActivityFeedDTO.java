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
public class TeamActivityFeedDTO {

    @Schema(name = "", example = "")
    private Long feedId;

    @Schema(name = "", example = "")
    private Long teamId;

    @Schema(name = "", example = "")
    private Long memberId;

    @Schema(name = "", example = "")
    private String activityType;

    @Schema(name = "", example = "")
    private String activityContent;

    @Schema(name = "", example = "")
    private LocalDateTime feedCreatedDate;
}