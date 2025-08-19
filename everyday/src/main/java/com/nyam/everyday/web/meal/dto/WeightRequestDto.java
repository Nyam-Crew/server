package com.nyam.everyday.web.meal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * WeightRequestDto
 *
 * @author : 장소희
 * @fileName : WeightRequestDto
 * @since : 25. 8. 7.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "체중 기록 요청 DTO")
public class WeightRequestDto {

    @Schema(description = "기록 날짜", example = "2025-08-19")
    private Date date;

    @Schema(description = "체중(kg)", example = "64.2")
    private Double weight;
}
