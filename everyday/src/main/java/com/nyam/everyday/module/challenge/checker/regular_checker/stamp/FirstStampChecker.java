package com.nyam.everyday.module.challenge.checker.regular_checker.stamp;

import com.nyam.everyday.module.challenge.checker.AbstractCountBasedChecker;
import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.challenge.repository.ChallengeRepository;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.mission.repository.DailyMissionStampRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class FirstStampChecker extends AbstractCountBasedChecker {

  private final DailyMissionStampRepository dailyMissionStampRepository;

  protected FirstStampChecker(
      ChallengeRepository challengeRepository,
      ChallengeCheckService challengeCheckService,
      ApplicationEventPublisher publisher,
      DailyMissionStampRepository dailyMissionStampRepository
  ) {
    super(challengeRepository, challengeCheckService, publisher);
    this.dailyMissionStampRepository = dailyMissionStampRepository;
  }

  @Override
  public ChallengeCode getChallengeCode() {
    return ChallengeCode.STAMP_FIRST;
  }

  @Override
  public ChallengeTag getChallengeTag() {
    return ChallengeTag.STAMP;
  }

  @Override
  public long getProgress(Member member, Challenge challenge) {
    // 도장 횟수를 반환한다.
    return dailyMissionStampRepository.getCountByMemberId(member.getMemberId());
  }
}
