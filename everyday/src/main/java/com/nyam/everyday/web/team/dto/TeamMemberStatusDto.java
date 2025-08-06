package com.nyam.everyday.web.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 그룹 참여 현황 DTO
 *
 * @author : 이지은
 * @fileName : TeamMemberStatusDTO
 * @since : 25. 8. 4.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberStatusDto {

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
    private LocalDateTime createdDate;

    @Schema(name = "", example = "")
    private LocalDateTime modifiedDate;
}