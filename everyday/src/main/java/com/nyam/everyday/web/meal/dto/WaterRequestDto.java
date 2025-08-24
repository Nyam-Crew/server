package com.nyam.everyday.web.meal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

/*
 * 물 섭취 기록 요청 DTO
 *
 * 설계 의도
 * - 하루 물 섭취 기록을 등록/수정할 때 사용
 * - date: 기록 대상 날짜
 * - amount: 섭취량(ml)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "물 섭취 기록 요청 DTO")
public class WaterRequestDto {

    @Schema(description = "섭취 날짜", example = "2025-08-19")
    private Date date;

    @Schema(description = "섭취량(ml)", example = "300")
    private BigDecimal amount;
}