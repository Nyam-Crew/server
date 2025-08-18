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
@Schema(description = "팀 랭킹 정보")
public class TeamRankingDto {

    @Schema(description = "팀 ID", example = "1")
    private Long teamId;

    @Schema(description = "팀 이름", example = "우리팀")
    private String teamName;

    @Schema(description = "팀 평균 점수", example = "75.5")
    private Double averageScore;

    @Schema(description = "팀 랭킹", example = "1")
    private Long rank;
}
