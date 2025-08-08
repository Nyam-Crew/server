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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDto {

    @Schema(description = "사용자 Id", example = "")
    private Long teamId;
    private String teamName;
    private String teamDescription;
    private int teamMaxMembers;
    private LocalDateTime teamCreatedDate;
    private LocalDateTime modifiedDate;
    private Long ownerId;
}