package com.nyam.everyday.web.team.dto;

import java.math.BigDecimal;
import lombok.*;

/**
 * 그룹 간 경쟁 관련 Dto
 *
 * @author : 이지은
 * @fileName : TeamGlobalRankingDto
 * @since : 25. 8. 6.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamGlobalRankingDto {

    private Long id;
    private Integer rankingYear;
    private Integer rankingMonth;
    private Long teamId;
    private String teamName;
    private Integer rank;
    private BigDecimal averageScore;
    private Long totalScore;
    private Integer memberCount;

}

