package com.nyam.everyday.web.summary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * MemberDailySummaryDto
 *
 * @author : 장소희
 * @fileName : MemberDailySummaryDto
 * @since : 25. 8. 7.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "회원 일일 요약 데이터 DTO")
public class MemberDailySummaryDto {

    @Schema(description = "회원 일일 요약 ID")
    private Long memberDailyId;

    @Schema(description = "회원 ID")
    private Long memberId;

    @Schema(description = "요약 등록일")
    private Date summaryDate;

    @Schema(description = "체중")
    private BigDecimal weight;

    @Schema(description = "총 단백질")
    private Integer totalProtein;

    @Schema(description = "총 탄수화물")
    private Integer totalCarbohydrate;

    @Schema(description = "총 지방")
    private Integer totalFat;

    @Schema(description = "총 물 섭취량")
    private Integer totalWater;

    @Schema(description = "총 칼로리")
    private BigDecimal totalKcal;

    @Schema(description = "생성일시")
    private LocalDateTime createdDate;

    @Schema(description = "수정일시")
    private LocalDateTime modifiedDate;
}
