package com.nyam.everyday.module.summary.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.summary.entity.MemberDailySummary;
import com.nyam.everyday.module.summary.repository.MemberDailySummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberDailySummaryService {

    private final MemberDailySummaryRepository summaryRepository;
    private final MemberRepository memberRepository;

    /** 물 섭취량 추가/수정 (오늘 summaryDate 기준) */
    @Transactional
    public void addOrUpdateWater(Long memberId, Integer amount) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        MemberDailySummary summary = summaryRepository
                .findByMember_MemberIdAndSummaryDate(memberId, today)
                .orElseGet(() -> MemberDailySummary.builder()
                        .member(member)
                        .summaryDate(today)
                        .weight(member.getWeight())              // null 가능
                        .totalProtein(BigDecimal.ZERO)           // g
                        .totalCarbohydrate(BigDecimal.ZERO)      // g
                        .totalFat(BigDecimal.ZERO)               // g
                        .totalWater(BigDecimal.ZERO)             // ml
                        .totalKcal(0)                            // kcal (정수)
                        .createdDate(now)
                        .modifiedDate(now)
                        .build()
                );

        // Integer -> BigDecimal로 세팅 (null이면 0)
        int water = amount != null ? amount : 0;
        summary.setTotalWater(BigDecimal.valueOf(water));
        summary.setModifiedDate(now);

        summaryRepository.save(summary);
    }

    /** 체중 추가/수정 (오늘 summaryDate 기준) */
    @Transactional
    public void addOrUpdateWeight(Long memberId, Double weight) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        MemberDailySummary summary = summaryRepository
                .findByMember_MemberIdAndSummaryDate(memberId, today)
                .orElseGet(() -> MemberDailySummary.builder()
                        .member(member)
                        .summaryDate(today)
                        .weight(null)                            // 아래에서 세팅
                        .totalProtein(BigDecimal.ZERO)
                        .totalCarbohydrate(BigDecimal.ZERO)
                        .totalFat(BigDecimal.ZERO)
                        .totalWater(BigDecimal.ZERO)
                        .totalKcal(0)
                        .createdDate(now)
                        .modifiedDate(now)
                        .build()
                );

        summary.setWeight(weight != null ? BigDecimal.valueOf(weight) : null);
        summary.setModifiedDate(now);

        summaryRepository.save(summary);
    }
}