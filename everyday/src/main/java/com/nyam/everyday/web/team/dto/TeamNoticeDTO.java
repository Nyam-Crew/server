package com.nyam.everyday.web.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 그룹 공지 DTO
 *
 * @author : 이지은
 * @fileName : TeamNoticeDTO
 * @since : 25. 8. 4.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamNoticeDTO {

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