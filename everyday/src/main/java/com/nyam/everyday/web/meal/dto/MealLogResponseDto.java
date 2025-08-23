package com.nyam.everyday.web.meal.dto;

import com.nyam.everyday.module.meal.type.MealType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

/*
 * 음식 섭취 기록 응답 DTO
 *
 * 설계 의도
 * - 클라이언트로 반환되는 음식 기록 단건 정보
 * - 음식명, 섭취량, 칼로리, 3대 영양소 등 포함
 * - 작성/수정 시각(LocalDateTime) 포함
 */
@Getter
@RequiredArgsConstructor
@Builder
@Schema(description = "음식 섭취 기록 응답 DTO")
public class MealLogResponseDto {

    @Schema(description = "음식 기록 PK")
    private final Long mealLogId;

    @Schema(description = "회원 PK")
    private final Long memberId;

    @Schema(description = "음식 PK")
    private final Long foodId;

    @Schema(description = "음식명")
    private final String foodName;

    @Schema(description = "섭취량 (g, ml 등)")
    private final Integer intakeAmount;

    @Schema(description = "섭취 칼로리")
    private final BigDecimal intakeKcal;

    @Schema(description = "식사 타입 (BREAKFAST:아침, LUNCH:점심, DINNER:저녁, SNACK:간식)")
    private final MealType mealType;

    @Schema(description = "기록 생성일시")
    private final LocalDateTime createdDate;

    @Schema(description = "기록 수정일시")
    private final LocalDateTime modifiedDate;
}