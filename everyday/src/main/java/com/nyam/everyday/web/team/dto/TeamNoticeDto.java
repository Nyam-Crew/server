package com.nyam.everyday.web.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 그룹 공지 DTO
 *
 * @author : 이지은
 * @fileName : TeamNoticeDTO
 * @since : 25. 8. 4.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamNoticeDto {

    @Schema(name = "", example = "")
    private Long teamNoticeId;

    @Schema(name = "", example = "")
    private Long teamId;

    @Schema(name = "", example = "")
    private Long memberId;

    @Schema(name = "", example = "")
    private String title;

    @Schema(name = "", example = "")
    private String content;

    @Schema(name = "", example = "")
    private LocalDateTime teamNoticeCreatedDate;

    @Schema(name = "", example = "")
    private LocalDateTime teamNoticeModifiedDate;
}