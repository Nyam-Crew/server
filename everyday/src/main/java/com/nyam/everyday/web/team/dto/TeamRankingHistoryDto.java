package com.nyam.everyday.web.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 그룹 내부 랭킹 기록 DTO
 *
 * @author : 이지은
 * @fileName : TeamRankingHistoryDTO
 * @since : 25. 8. 4.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamRankingHistoryDto {

    @Schema(name = "", example = "")
    private Long teamRankingId;

    @Schema(name = "", example = "")
    private Long teamId;

    @Schema(name = "", example = "")
    private Long memberId;

    @Schema(name = "", example = "")
    private String weekCode;

    @Schema(name = "", example = "")
    private int point;

    @Schema(name = "", example = "")
    private String field;

    @Schema(name = "", example = "")
    private LocalDateTime teamRankingCreatedDate;
}