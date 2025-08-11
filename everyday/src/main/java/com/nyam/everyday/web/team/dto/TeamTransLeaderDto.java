package com.nyam.everyday.web.team.dto;

import lombok.*;

/**
 *
 * 방장 권한 부여 관련 Dto
 *
 * @author : 이지은
 * @fileName : TeamTransLeaderDto
 * @since : 25. 8. 11.
 *
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamTransLeaderDto {
    private Long targetMemberId;
}