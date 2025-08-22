package com.nyam.everyday.module.summary.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.scorelog.service.ScoreAwardService;
import com.nyam.everyday.module.summary.entity.MemberDailySummary;
import com.nyam.everyday.module.summary.repository.MemberDailySummaryRepository;
import com.nyam.everyday.module.team.enums.ActivityType;
import com.nyam.everyday.module.team.service.TeamActivityFeedService;
import com.nyam.everyday.module.team.service.TeamMemberService;
import com.nyam.everyday.module.team.util.FeedIds;
import com.nyam.everyday.web.team.dto.TeamActivityFeedItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberDailySummaryService {

    private final MemberDailySummaryRepository summaryRepository;
    private final MemberRepository memberRepository;

    private final TeamActivityFeedService feedService;
    private final TeamMemberService teamMemberService;
    private final ScoreAwardService scoreAwardService;

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

        // ✅ 물 섭취 피드 발행 메서드 호출
        try {
            publishWaterFeed(savedSummary);
        } catch (Exception e) {
            // 부가 기능(피드/알림)의 실패가 핵심 기능(물 기록 저장)에 영향을 주지 않도록 격리
            log.error("[물 섭취 피드/알림 발행 실패] 물 기록은 정상 저장되었으나, 피드/알림 생성 중 예외가 발생했습니다. memberId: {}, summaryId: {}. Error: {}",
                    memberId, savedSummary.getMemberDailyId(), e.getMessage(), e);
        }

        // ✅ 물 기록 점수 부여 로직 호출
        // amount가 0보다 클 때만 호출하는 등의 조건을 걸 필요가 없다.
        // ScoreAwardService가 이미 "하루 한 번"만 주도록 처리하기 때문
        if (amount != null && amount > 0) { // 단, 물을 마시지 않았는데 점수를 주는 것을 방지하기 위해 0 초과 조건 추가
            scoreAwardService.awardWaterDailyOnce(member);
        }
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

        // ✅ 체중 변화 피드 발행 메서드 호출
        try {
            publishWeightFeed(savedSummary);
        } catch (Exception e) {
            // 부가 기능(피드/알림)의 실패가 핵심 기능(체중 기록 저장)에 영향을 주지 않도록 격리
            log.error("[체중 기록 피드/알림 발행 실패] 체중 기록은 정상 저장되었으나, 피드/알림 생성 중 예외가 발생했습니다. memberId: {}, summaryId: {}. Error: {}",
                    memberId, savedSummary.getMemberDailyId(), e.getMessage(), e);
        }

        // ✅ 체중 기록 점수 부여 로직 호출
        // weight가 null이 아닐 때(즉, 기록이 삭제된 경우가 아닐 때) 호출합니다.
        // "최초 한 번"만 주는 로직은 ScoreAwardService가 알아서 처리합니다.
        if (weight != null) {
            scoreAwardService.awardWeightDailyOnce(member);
        }
    }

    // =================================================================
    // ✅ 물 섭취 피드를 발행하는 private 메서드
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

    //Todo.[Refactor] 중간에 중복로직이 있는데 혹시 줄이는 쪽으로 리팩터링 하신다면 아래 메서드를 쓰시면 됩니다.

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