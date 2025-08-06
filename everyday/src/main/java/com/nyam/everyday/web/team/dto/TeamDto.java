package com.nyam.everyday.web.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 그룹 CRUD DTO
 *
 * @author : 이지은
 * @fileName : TeamDTO
 * @since : 25. 8. 4.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDto {
    @Schema(name = "", example = "")
    private Long teamId;

    @Schema(name = "", example = "")
    private String teamName;

    @Schema(name = "", example = "")
    private String teamDescription;

    @Schema(name = "", example = "")
    private int teamMaxMembers;

    @Schema(name = "", example = "")
    private LocalDateTime createdDate;

    @Schema(name = "", example = "")
    private LocalDateTime modifiedDate;

    @Schema(name = "", example = "")
    private Long ownerId;
}