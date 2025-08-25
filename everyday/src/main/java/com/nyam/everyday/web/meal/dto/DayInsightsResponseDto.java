package com.nyam.everyday.web.meal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/*
 * 하루 분석(인사이트) 응답 DTO
 *
 * 설계 의도
 * - 하루 동안의 건강 지표와 섭취 요약 데이터를 통합 반환
 * - null 허용 값: 계산 불가하거나 미입력 시 null
 * - 요약 값(total*): 데이터 없을 경우 0으로 기본 처리
 */
@Data
@Builder
@Schema(description = "하루 분석(인사이트) 응답 DTO")
public class DayInsightsResponseDto {

    @Schema(description = "회원 PK")
    private Long memberId;

    @Schema(description = "회원 닉네임")
    private String nickname;

    @Schema(description = "나이")
    private Integer age;

    @Schema(description = "체질량지수 (BMI), null 허용")
    private BigDecimal bmi;

    @Schema(description = "기초대사량 (BMR), null 허용")
    private BigDecimal bmr;

    @Schema(description = "하루 총에너지소비량 (TDEE), null 허용")
    private BigDecimal tdee;

    @Schema(description = "권장 섭취 칼로리, null 허용")
    private Integer recommendedCalories;

    @Schema(description = "하루 단백질 섭취 합계 (없으면 0)")
    private BigDecimal totalProtein;

    @Schema(description = "하루 탄수화물 섭취 합계 (없으면 0)")
    private BigDecimal totalCarbohydrate;

    @Schema(description = "하루 지방 섭취 합계 (없으면 0)")
    private BigDecimal totalFat;

    @Schema(description = "하루 물 섭취 합계 (없으면 0)")
    private BigDecimal totalWater;

    @Schema(description = "하루 섭취 칼로리 합계 (없으면 0)")
    private BigDecimal totalKcal;

    @Schema(description = "프로필 체중: member.weight (가입 시/프로필에 저장된 기준 체중)")
    private BigDecimal profileWeight;

    @Schema(description = "오늘의 체중: member_daily_summary.weight (요약 테이블의 해당 날짜 체중)")
    private BigDecimal todayWeight;
}