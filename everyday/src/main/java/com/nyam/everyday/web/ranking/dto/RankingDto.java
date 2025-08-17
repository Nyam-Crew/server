package com.nyam.everyday.web.ranking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "랭크 정보")
public class RankingDto {

    @Schema(description = "member ID", example = "101")
    private Long memberId;

    @Schema(description = "nickname", example = "TopPlayer1")
    private String nickname;

    @Schema(description = "total Score", example = "15000.0")
    private Double totalScore;

    @Schema(description = "rank", example = "1")
    private Long rank;
}
