package com.nyam.everyday.module.meal.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.common.util.HealthCalculator;
import com.nyam.everyday.common.util.HealthCalculator.HealthInfo;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.summary.entity.MemberDailySummary;
import com.nyam.everyday.module.summary.repository.MemberDailySummaryRepository;
import com.nyam.everyday.web.meal.dto.DayInsightsResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;          // ✅ java.sql.Date
import java.time.LocalDate;

@Service
@Slf4j
@RequiredArgsConstructor
public class MealInsightsService {

    private final MemberRepository memberRepository;
    private final MemberDailySummaryRepository dailySummaryRepository;

    public DayInsightsResponseDto getDayInsights(Long memberId, LocalDate date) {
        // 1) 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "id " + memberId + " 회원이 없습니다."));

        // 2) 건강지표 계산 (조건 충족 시)
        BigDecimal bmi = null, bmr = null, tdee = null;
        Integer recommendedCalories = null;

        try {
            boolean hasHeight = member.getHeight() != null && member.getHeight().compareTo(BigDecimal.ZERO) > 0;
            boolean hasWeight = member.getWeight() != null && member.getWeight().compareTo(BigDecimal.ZERO) > 0;
            boolean hasValidAge = member.getAge() > 5;

            if (hasHeight && hasWeight && hasValidAge) {
                HealthInfo info = HealthCalculator.calculate(member);
                if (info != null) {
                    bmi  = info.bmi();
                    bmr  = info.bmr();
                    tdee = info.tdee();

                    if (member.getTargetWeight() != null && tdee != null && tdee.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal currentWeight = member.getWeight();
                        BigDecimal targetWeight  = member.getTargetWeight();
                        BigDecimal rec = (targetWeight.compareTo(currentWeight) < 0)
                                ? tdee.subtract(new BigDecimal("500"))
                                : (targetWeight.compareTo(currentWeight) > 0)
                                ? tdee.add(new BigDecimal("500"))
                                : tdee;

                        recommendedCalories = rec.setScale(0, RoundingMode.HALF_UP).intValue();
                        if (recommendedCalories < 0) recommendedCalories = 0; // 안전망
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Health calculation failed. memberId={}, cause={}", memberId, e.getMessage());
        }

        // 3) 하루 요약 조회 (없으면 0으로)
        Date sqlDate = Date.valueOf(date); // ✅ LocalDate → java.sql.Date
        MemberDailySummary summary = dailySummaryRepository
                .findByMember_MemberIdAndSummaryDate(memberId, sqlDate) // ✅ 레포는 java.sql.Date 받음
                .orElse(null);

        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal totalProtein      = (summary != null && summary.getTotalProtein() != null) ? summary.getTotalProtein() : zero;
        BigDecimal totalCarbohydrate = (summary != null && summary.getTotalCarbohydrate() != null) ? summary.getTotalCarbohydrate() : zero;
        BigDecimal totalFat          = (summary != null && summary.getTotalFat() != null) ? summary.getTotalFat() : zero;
        BigDecimal totalWater        = (summary != null && summary.getTotalWater() != null) ? summary.getTotalWater() : zero;
        BigDecimal totalKcal         = (summary != null && summary.getTotalKcal() != null) ? summary.getTotalKcal() : zero;

        // 4) 응답
        return DayInsightsResponseDto.builder()
                .memberId(member.getMemberId())
                .nickname(member.getNickname())
                .age(member.getAge())
                .bmi(bmi)
                .bmr(bmr)
                .tdee(tdee)
                .recommendedCalories(recommendedCalories)
                .totalProtein(totalProtein)
                .totalCarbohydrate(totalCarbohydrate)
                .totalFat(totalFat)
                .totalWater(totalWater)
                .totalKcal(totalKcal)
                .build();
    }
}