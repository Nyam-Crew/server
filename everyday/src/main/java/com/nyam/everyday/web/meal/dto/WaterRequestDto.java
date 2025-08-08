package com.nyam.everyday.web.meal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * WaterRequestDto
 *
 * @author : 장소희
 * @fileName : WaterRequestDto
 * @since : 25. 8. 7.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "물 섭취 기록 요청 DTO")
public class WaterRequestDto {
    @Schema(description = "섭취량(ml)", example = "300")
    private Integer amount;
}
