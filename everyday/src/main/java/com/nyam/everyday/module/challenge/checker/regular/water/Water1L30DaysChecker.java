package com.nyam.everyday.module.challenge.checker.regular.water;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.module.challenge.checker.ChallengeChecker;
import com.nyam.everyday.module.challenge.checker.event.event.MCDCreateEvent;
import com.nyam.everyday.module.challenge.checker.event.event.MCDDeleteEvent;
import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.summary.entity.MemberDailySummary;
import com.nyam.everyday.module.summary.repository.MemberDailySummaryRepository;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Water1L30DaysChecker implements ChallengeChecker {

  private final MemberDailySummaryRepository memberDailySummaryRepository;
  private final ChallengeCheckService challengeCheckService;
  private final ApplicationEventPublisher publisher;

  @Override
  public ChallengeCode getChallengeCode() {
    return ChallengeCode.WATER_1L_30DAYS;
  }

  @Override
  public ChallengeTag getChallengeTag() {
    return ChallengeTag.WATER;
  }

  @Override
  public void check(Member member, LocalDate targetDate) {
    // 챌린지 정보 가져오기
    Challenge challenge = challengeCheckService.getChallengeByChallengeCode(
        this.getChallengeCode());

    // 체크해야 할 챌린지인지 확인
    if (!challengeCheckService.needToCheckChallenge(member, challenge)) {
      return;
    }

    // 멤버의 해당 날짜 기록 가져오기
    MemberDailySummary mds = memberDailySummaryRepository.findByMember_MemberIdAndSummaryDate(
            member.getMemberId(),
            Date.valueOf(LocalDate.now()))
        .orElseThrow(() -> BaseException.MEMBER_DAILY_SUMMARY_NOT_FOUND);

    // 조건 달성했는지 확인 (그날 물 1L 이상 마심)
    if (mds.getTotalWater().compareTo(BigDecimal.valueOf(1000)) >= 0) {
      // 생성 이벤트 발행
      publisher.publishEvent(new MCDCreateEvent(member, challenge, targetDate));
    }

    // 조건을 달성하지 못하게 되었는지 확인 (그날 물 1L 이하로 마심)
    else {
      // 삭제 이벤트 발행
      publisher.publishEvent(new MCDDeleteEvent(member, challenge, targetDate));
    }
  }

  @Override
  public Boolean isSatisfied(Integer progressCount) {
    // 30회 이상 1L 넘게 물을 마셨으면, 달성
    return progressCount >= 30;
  }
}
