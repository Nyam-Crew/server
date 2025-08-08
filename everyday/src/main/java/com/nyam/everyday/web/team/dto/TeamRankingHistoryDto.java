package com.nyam.everyday.web.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 그룹 내부 랭킹 기록 DTO
 *
 * @author : 이지은
 * @fileName : TeamRankingHistoryDTO
 * @since : 25. 8. 4.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamRankingHistoryDTO {

    @Schema(description = "사용자 이메일", example = "ssj@naver.com")
    private Long id;
    private Long groupId;
    private Long memberId;
    private String weekCode;
    private int point;
    private String field;
    private LocalDateTime createdAt;
}