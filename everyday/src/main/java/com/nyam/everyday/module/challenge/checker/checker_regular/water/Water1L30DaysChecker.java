package com.nyam.everyday.module.challenge.checker.checker_regular.water;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.module.challenge.checker.AbstractDateBasedChecker;
import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.challenge.repository.ChallengeRepository;
import com.nyam.everyday.module.challenge.repository.MemberChallengeDayRepository;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.summary.entity.MemberDailySummary;
import com.nyam.everyday.module.summary.repository.MemberDailySummaryRepository;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class Water1L30DaysChecker extends AbstractDateBasedChecker {

  private final MemberDailySummaryRepository memberDailySummaryRepository;

  protected Water1L30DaysChecker(
          ChallengeRepository challengeRepository,
          ChallengeCheckService challengeCheckService,
          MemberChallengeDayRepository memberChallengeDayRepository,
          ApplicationEventPublisher publisher,
          MemberDailySummaryRepository memberDailySummaryRepository
  ) {
    super(challengeRepository, challengeCheckService, memberChallengeDayRepository, publisher);
    this.memberDailySummaryRepository = memberDailySummaryRepository;
  }

  @Override
  public ChallengeCode getChallengeCode() {
    return ChallengeCode.WATER_1L_30DAYS;
  }

  @Override
  public ChallengeTag getChallengeTag() {
    return ChallengeTag.WATER;
  }

  @Override
  protected void doCheckAndPublish(Member member, Challenge challenge, LocalDate targetDate) {
    // 멤버의 해당 날짜 기록 가져오기
    MemberDailySummary mds = memberDailySummaryRepository.findByMember_MemberIdAndSummaryDate(
                    member.getMemberId(),
                    Date.valueOf(targetDate))
            .orElseThrow(() -> BaseException.MEMBER_DAILY_SUMMARY_NOT_FOUND);

    // 조건 달성했는지 확인 (그날 물 1L 이상 마심)
    if (mds.getTotalWater().compareTo(BigDecimal.valueOf(1000)) >= 0) {
      // 생성 이벤트 발행
      publishMCDCreateEvent(member, challenge,  targetDate);
    }

    // 조건을 달성하지 못하게 되었는지 확인 (그날 물 1L 이하로 마심)
    else {
      // 삭제 이벤트 발행
      publishMCDDeleteEvent(member, challenge, targetDate);
    }
  }
}
