package com.nyam.everyday.web.meal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * MealLogRequestDto
 *
 * @author : 장소희
 * @fileName : MealLogRequestDto
 * @since : 25. 8. 5.
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "음식 섭취 기록 등록/수정 요청 DTO")
public class MealLogRequestDto {

    @Schema(description = "회원 PK")
    private Long memberId; // JWT 인증 기반이면 서버에서 추출, 프론트에선 omit 가능

    @Schema(description = "음식 PK")
    private Long foodId;

    @Schema(description = "섭취량 (g, ml 등)")
    private Integer intakeAmount;

    @Schema(description = "섭취 칼로리")
    private Double intakeKcal;

    @Schema(description = "식사 타입 (BREAKFAST:아침, LUNCH:점심, DINNER:저녁, SNACK:간식)")
    private String mealType;
}