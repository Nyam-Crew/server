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

/**
 * MemberDailySummaryService
 *
 * @author : 장소희
 * @fileName : MemberDailySummaryService
 * @since : 25. 8. 7.
 */

@Service
@RequiredArgsConstructor
public class MemberDailySummaryService{
    private final MemberDailySummaryRepository summaryRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void addOrUpdateWater(Long memberId, Integer amount) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        // 회원 엔티티 조회 (체중 정보 얻기 위해)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        MemberDailySummary summary = summaryRepository
                .findByMember_MemberIdAndCreatedDateBetween(memberId, startOfDay, endOfDay)
                .orElseGet(() -> {
                    MemberDailySummary newSummary = new MemberDailySummary();
                    newSummary.setMember(member);
                    newSummary.setWeight(member.getWeight()); // 회원 체중 넣기
                    newSummary.setTotalWater(0);
                    newSummary.setTotalProtein(0);
                    newSummary.setTotalCarbohydrate(0);
                    newSummary.setTotalFat(0);
                    newSummary.setTotalKcal(BigDecimal.ZERO);
                    newSummary.setCreatedDate(LocalDateTime.now());
                    newSummary.setModifiedDate(LocalDateTime.now());
                    return newSummary;
                });
        summary.setTotalWater(amount);
        summary.setModifiedDate(LocalDateTime.now());

        summaryRepository.save(summary);
    }

    @Transactional
    public void addOrUpdateWeight(Long memberId, Double weight) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        MemberDailySummary summary = summaryRepository
                .findByMember_MemberIdAndCreatedDateBetween(memberId, startOfDay, endOfDay)
                .orElseGet(() -> {
                    MemberDailySummary newSummary = new MemberDailySummary();
                    newSummary.setMember(member);
                    newSummary.setWeight(BigDecimal.valueOf(weight));
                    newSummary.setTotalProtein(0);
                    newSummary.setTotalCarbohydrate(0);
                    newSummary.setTotalFat(0);
                    newSummary.setTotalWater(0);
                    newSummary.setTotalKcal(BigDecimal.valueOf(0));
                    newSummary.setCreatedDate(LocalDateTime.now());
                    newSummary.setModifiedDate(LocalDateTime.now());
                    return newSummary;
                });

        // 덮어쓰기
        summary.setWeight(BigDecimal.valueOf(weight));
        summary.setModifiedDate(LocalDateTime.now());

        summaryRepository.save(summary);

        member.setWeight(BigDecimal.valueOf(weight));
        memberRepository.save(member);
    }

}
