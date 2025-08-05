package com.nyam.everyday.web.groub.dto;

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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDTO {
    @Schema(name = "", example = "")
    private Long teamId;

    @Schema(name = "", example = "")
    private String teamName;

    @Schema(name = "", example = "")
    private String teamDescription;

    @Schema(name = "", example = "")
    private int teamMaxMembers;

    @Schema(name = "", example = "")
    private LocalDateTime teamCreatedDate;

    @Schema(name = "", example = "")
    private LocalDateTime teamModifiedDate;

    @Schema(name = "", example = "")
    private Long ownerId;
}