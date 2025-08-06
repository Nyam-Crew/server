package com.nyam.everyday.web.team.dto;

import lombok.*;
import java.time.LocalDateTime;

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
    private String periodType;
    private String periodValue;
    private Long teamId;
    private Integer totalScore;
    private Integer rank;
    private LocalDateTime recordedAt;
}