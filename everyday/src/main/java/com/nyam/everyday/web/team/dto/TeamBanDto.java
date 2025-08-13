package com.nyam.everyday.web.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 *
 * 그룹 벤 관련 dto
 *
 * @author : 이지은
 * @fileName : TeamBenDto
 * @since : 25. 8. 11.
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamBanDto {
    @Schema(description = "강퇴 사유", example = "욕설 및 부적절한 언행")
    private String reason;
}