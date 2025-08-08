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
public class TeamDTO {

    @Schema(description = "사용자 이메일", example = "ssj@naver.com")
    private Long teamId;
    private String teamName;
    private String teamDescription;
    private int teamMaxMembers;
    private LocalDateTime teamCreatedAt;
    private LocalDateTime modifiedDate;
    private Long ownerId;
}