package com.nyam.everyday.module.summary.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.summary.entity.MemberDailySummary;
import com.nyam.everyday.module.summary.repository.MemberDailySummaryRepository;
import com.nyam.everyday.module.team.enums.ActivityType;
import com.nyam.everyday.module.team.service.TeamActivityFeedService;
import com.nyam.everyday.module.team.service.TeamMemberService;
import com.nyam.everyday.module.team.util.FeedIds;
import com.nyam.everyday.web.team.dto.TeamActivityFeedItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MemberDailySummaryService {

    private final MemberDailySummaryRepository summaryRepository;
    private final MemberRepository memberRepository;

    private final TeamActivityFeedService feedService;
    private final TeamMemberService teamMemberService; // ✅ 의존성 주입

    /** 물 섭취량 추가/수정 (오늘 summaryDate 기준) */
    @Transactional
    public void addOrUpdateWater(Long memberId, Integer amount, Date date) {

        LocalDateTime now = LocalDateTime.now();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        MemberDailySummary summary = summaryRepository
                .findByMember_MemberIdAndSummaryDate(memberId, date)
                .orElseGet(() -> MemberDailySummary.builder()
                        .member(member)
                        .summaryDate(date)
                        .weight(member.getWeight())              // null 가능
                        .totalProtein(BigDecimal.ZERO)           // g
                        .totalCarbohydrate(BigDecimal.ZERO)      // g
                        .totalFat(BigDecimal.ZERO)               // g
                        .totalWater(BigDecimal.ZERO)             // ml
                        .totalKcal(BigDecimal.ZERO)                            // kcal (정수)
                        .createdDate(now)
                        .modifiedDate(now)
                        .build()
                );

        // Integer -> BigDecimal로 세팅 (null이면 0)
        int water = amount != null ? amount : 0;
        summary.setTotalWater(BigDecimal.valueOf(water));
        summary.setModifiedDate(now);

        MemberDailySummary savedSummary = summaryRepository.save(summary);

        // ✅ 마지막에 물 섭취 피드 발행 메서드 호출
        publishWaterFeed(savedSummary);
    }

    /** 체중 추가/수정 (오늘 summaryDate 기준) */
    @Transactional
    public void addOrUpdateWeight(Long memberId, Double weight, Date date) {
        LocalDateTime now = LocalDateTime.now();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        MemberDailySummary summary = summaryRepository
                .findByMember_MemberIdAndSummaryDate(memberId, date)
                .orElseGet(() -> MemberDailySummary.builder()
                        .member(member)
                        .summaryDate(date)
                        .weight(null)                            // 아래에서 세팅
                        .totalProtein(BigDecimal.ZERO)
                        .totalCarbohydrate(BigDecimal.ZERO)
                        .totalFat(BigDecimal.ZERO)
                        .totalWater(BigDecimal.ZERO)
                        .totalKcal(BigDecimal.ZERO)
                        .createdDate(now)
                        .modifiedDate(now)
                        .build()
                );

        summary.setWeight(weight != null ? BigDecimal.valueOf(weight) : null);
        summary.setModifiedDate(now);

        MemberDailySummary savedSummary = summaryRepository.save(summary);

        // ✅ 마지막에 물 섭취 피드 발행 메서드 호출
        publishWeightFeed(savedSummary);
    }

    // =================================================================
    // ✅ [신규] 물 섭취 피드를 발행하는 private 메서드
    // =================================================================
    private void publishWaterFeed(MemberDailySummary summary) {
        Long memberId = summary.getMember().getMemberId();
        Set<Long> teamIds = teamMemberService.findTeamIdsByMember(memberId);
        if (teamIds == null || teamIds.isEmpty()) return;

        // getMemberDailyId()로 필드명 변경
        String feedId = FeedIds.water(memberId, summary.getMemberDailyId());

        if (summary.getTotalWater() == null || summary.getTotalWater().compareTo(BigDecimal.ZERO) <= 0) {
            feedService.removeFeedItem(teamIds, feedId);
            return;
        }

        long createdAtMs = summary.getModifiedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Member member = summary.getMember();

        TeamActivityFeedItem feedItem = TeamActivityFeedItem.builder()
                .feedId(feedId)
                .memberId(memberId)
                .nickname(member.getNickname())
                .profileImageUrl(member.getMemberImg())
                .activityType(ActivityType.WATER)
                .amountMl(summary.getTotalWater().intValue())
                .build();

        feedService.addFeedItemToTeams(teamIds, feedId, createdAtMs, feedItem, Duration.ofDays(3));
    }

    // =================================================================
    // ✅ [신규] 체중 기록 피드를 발행하는 private 메서드
    // =================================================================
    private void publishWeightFeed(MemberDailySummary summary) {
        Long memberId = summary.getMember().getMemberId();
        Set<Long> teamIds = teamMemberService.findTeamIdsByMember(memberId);
        if (teamIds == null || teamIds.isEmpty()) return;

        // getMemberDailyId()로 필드명 변경
        String feedId = FeedIds.weight(memberId, summary.getMemberDailyId());
        Member member = summary.getMember();

        if (summary.getWeight() == null) {
            feedService.removeFeedItem(teamIds, feedId);
            return;
        }

        Double deltaKg = null;
        if (member.getWeight() != null) {
            // member.getWeight()도 BigDecimal이라 가정
            deltaKg = summary.getWeight().subtract(member.getWeight()).doubleValue();
        }

        long createdAtMs = summary.getModifiedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        TeamActivityFeedItem feedItem = TeamActivityFeedItem.builder()
                .feedId(feedId)
                .memberId(memberId)
                .nickname(member.getNickname())
                .profileImageUrl(member.getMemberImg())
                .activityType(ActivityType.WEIGHT)
                .weightKg(summary.getWeight().doubleValue())
                .deltaKg(deltaKg)
                .build();

        feedService.addFeedItemToTeams(teamIds, feedId, createdAtMs, feedItem, Duration.ofDays(3));
    }

    // =================================================================
    // ✅ addOrUpdateWater(),addOrUpdateWeight() 중간에 중복 코드 제거하실 거면 쓰세용
    // =================================================================
//    private MemberDailySummary createNewSummary(Member member, Date summaryDate, LocalDateTime now) {
//        return MemberDailySummary.builder()
//                .member(member)
//                .summaryDate(summaryDate)
//                .weight(member.getWeight())
//                .totalProtein(BigDecimal.ZERO)
//                .totalCarbohydrate(BigDecimal.ZERO)
//                .totalFat(BigDecimal.ZERO)
//                .totalWater(BigDecimal.ZERO)
//                .totalKcal(BigDecimal.ZERO)
//                .createdDate(now)
//                .modifiedDate(now)
//                .build();
//    }


}