package com.nyam.everyday.web.groub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 그룹 참여 현황 DTO
 *
 * @author : 이지은
 * @fileName : TeamMemberStatusDTO
 * @since : 25. 8. 4.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberStatusDTO {

    @Schema(name = "", example = "")
    private Long teamMemberId;

    @Schema(name = "", example = "")
    private Long teamId;

    @Schema(name = "", example = "")
    private Long memberId;

    @Schema(name = "", example = "")
    private String status;

    @Schema(name = "", example = "")
    private String teamRole;

    @Schema(name = "", example = "")
    private LocalDateTime teamStatusCreatedDate;

    @Schema(name = "", example = "")
    private LocalDateTime teamStatusModifiedDate;
}