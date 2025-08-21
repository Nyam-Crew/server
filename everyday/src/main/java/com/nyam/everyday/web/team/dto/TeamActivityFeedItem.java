package com.nyam.everyday.web.team.dto;

import com.nyam.everyday.module.meal.type.MealType;
import com.nyam.everyday.module.team.enums.ActivityType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 그룹 실시간 피드 DTO
 *
 * @author : 이지은
 * @fileName : TeamActivityFeedDTO
 * @since : 25. 8. 4.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamActivityFeedItem{

    @Schema(name = "피드 ID", example = "MEAL:{memberId}:{mealLogId}")
    private String feedId;//도메인 기반 id

    @Schema(description = "그룹 ID", example = "1")
    private Long teamId;

    @Schema(description = "그룹 멤버 ID", example = "1")
    private Long memberId;

    @Schema(description = "작성자 닉네임", example = "민수")
    private String nickname;

    @Schema(description = "작성자 프로필 이미지", example = "https://.../profile.jpg")
    private String profileImageUrl;

    @Schema(description = "활동 타입", example = "WATER")
    private ActivityType activityType;

    @Schema(description = "활동 내용", example = "200ml를 마셨습니다.")
    private String activityMessage;

    @Schema(name = "피드 생성 시간")
    private LocalDateTime feedCreatedDate;

    @Schema(description = "수정 여부", example = "false")
    private boolean isModified;

    // === 타입별 확장 필드 ===

    @Schema(description = "물 기록(ml)", example = "500")
    private Integer amountMl; // WATER 전용

    @Schema(description = "식단 구분", example = "MORNING")
    private MealType mealPeriod; // MEAL 전용 (아침/점심/저녁/간식)

    @Schema(description = "식단 총 칼로리", example = "250")
    private BigDecimal kcal; // MEAL 전용

//    @Schema(description = "식단 메뉴", example = "닭가슴살 샐러드")
//    private String menu; // MEAL 전용

    @Schema(description = "식단 썸네일 URL")
    private String thumbnailUrl; // MEAL 전용

    @Schema(description = "체중 기록(kg)", example = "70.5")
    private Double weightKg; // WEIGHT 전용

    @Schema(description = "체중 변화량(kg)", example = "-0.5")
    private Double deltaKg; // WEIGHT 전용

    @Schema(description = "챌린지명", example = "1일 2L 물마시기")
    private String challengeName; // CHALLENGE 전용
}