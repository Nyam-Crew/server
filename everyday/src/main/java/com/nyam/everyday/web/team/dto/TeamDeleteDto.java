package com.nyam.everyday.web.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 *
 * 팀 삭제 Dto
 *
 * @author : 이지은
 * @fileName : TeamDeleteDto
 * @since : 25. 8. 11.
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDeleteDto {
    @Schema(description = "팀삭제 확인용 팀명(정확히 일치해야 함)", example = "운동하는 사자들", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmTeamTitle;
}