package com.nyam.everyday.web.team.dto;

import com.nyam.everyday.module.team.enums.ParticipationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 *
 * team_member_status 정보만 있는 dto
 *
 * @author : 이지은
 * @fileName : MemberStatusUpdateDto
 * @since : 25. 8. 7.
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberStatusUpdateDto {

    @Schema(description = "변경할 참여 상태", example = "APPROVED")
    private ParticipationStatus status;
}