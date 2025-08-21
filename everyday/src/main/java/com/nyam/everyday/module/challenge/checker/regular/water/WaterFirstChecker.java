package com.nyam.everyday.module.challenge.checker.regular.water;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.module.challenge.checker.ChallengeChecker;
import com.nyam.everyday.module.challenge.checker.event.event.MCDCreateEvent;
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
public class WaterFirstChecker implements ChallengeChecker {

  private final ChallengeCheckService challengeCheckService;
  private final MemberDailySummaryRepository memberDailySummaryRepository;
  private final ApplicationEventPublisher publisher;

  @Override
  public ChallengeCode getChallengeCode() {
    return ChallengeCode.WATER_FIRST;
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

    // 챌린지 체크할 필요 없으면 아무것도 하지 않음
    if (!challengeCheckService.needToCheckChallenge(member, challenge)) {
      return;
    }

    // 멤버의 특정 날짜의 데일리 기록 가져오기
    MemberDailySummary mds = memberDailySummaryRepository.findByMember_MemberIdAndSummaryDate(
            member.getMemberId(), Date.valueOf(targetDate))
        .orElseThrow(() -> BaseException.MEMBER_DAILY_SUMMARY_NOT_FOUND);

    // 물을 조금이라도 마셨을 경우
    if (mds.getTotalWater().compareTo(BigDecimal.valueOf(0)) > 0) {
      // MCD 생성 이벤트 발행
      publisher.publishEvent(new MCDCreateEvent(member, challenge, targetDate));
    }
  }

  @Override
  public Boolean isSatisfied(Integer progressCount) {
    // 한번 이상 물을 마셨으면 달성
    return progressCount > 0;
  }
}