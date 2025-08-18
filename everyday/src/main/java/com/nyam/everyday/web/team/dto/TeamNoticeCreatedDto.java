package com.nyam.everyday.web.team.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 *
 * 그룹 생성에 사용하는 DTO
 *
 * @author : 이지은
 * @fileName : TeamNoticeCreatedDto
 * @since : 25. 8. 12.
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamNoticeCreatedDto {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
}