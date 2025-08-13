package com.nyam.everyday.web.team.dto;

import com.nyam.everyday.module.team.enums.TeamRole;
import lombok.*;

/**
 *
 * 그룹 역할 변경 Dto
 *
 * @author : 이지은
 * @fileName : TeamRoleChangeDto
 * @since : 25. 8. 11.
 *
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamRoleChangeDto {
    private Long targetMemberId;
    private TeamRole role; // SUBLEADER or MEMBER
}