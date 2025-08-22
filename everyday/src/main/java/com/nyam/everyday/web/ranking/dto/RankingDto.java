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
@Schema(description = "개인 랭킹 정보")
public class RankingDto {

    @Schema(description = "member ID", example = "101")
    private Long memberId;

    @Schema(description = "nickname", example = "TopPlayer1")
    private String nickname;

    @Schema(description = "total Score", example = "15000.0")
    private Double totalScore;

    @Schema(description = "rank", example = "1")
    private Long rank;

    @Schema(description = "이전 기간 대비 순위 변화(+n 상승, -n 하락, null: 이전 데이터 없음)", example = "+2")
    private Integer rankDelta;
}
