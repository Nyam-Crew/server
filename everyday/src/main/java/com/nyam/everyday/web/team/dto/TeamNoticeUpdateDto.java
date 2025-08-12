package com.nyam.everyday.web.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 *
 *
 * @author : 이지은
 * @fileName : TeamNoticeUpdateDto
 * @since : 25. 8. 12.
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamNoticeUpdateDto {

    @Size(max = 100)
    private String title;     // null이면 수정 안함

    @Size(max = 5000)
    private String content;
}