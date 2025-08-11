package com.nyam.everyday.web.team.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 그룹 정보 업데이트 관련 DTO
 *
 * @author : 이지은
 * @fileName : TeamUpdateDto
 * @since : 25. 8. 8.
 *
 */
@Getter
@Setter
public class TeamUpdateDto {
    @Size(min = 1, max = 10)
    private String teamTitle;

    @Size(max = 500)
    private String teamDescription;

    @Min(1)
    private Integer maxMembers; // null이면 그대로
}