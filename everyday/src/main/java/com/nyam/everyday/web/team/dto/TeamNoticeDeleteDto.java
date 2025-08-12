package com.nyam.everyday.web.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 *
 * 그룹 삭제 DTO
 *
 * @author : 이지은
 * @fileName : TeamNoticeDeleteDto
 * @since : 25. 8. 12.
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamNoticeDeleteDto {
    @Schema(description = "삭제 확인용 팀 제목(정확히 일치해야 함)", example = "운동하는 사자들")
    private String confirmTeamTitle;

    @Schema(description = "하드 삭제 여부(기본 false: 소프트 삭제가 있다면 활용). 현재는 하드 삭제만 구현.", example = "true")
    private Boolean hard; // 선택값. 지금은 null/false라도 하드삭제만 수행하도록 Service에서 처리 가능
}