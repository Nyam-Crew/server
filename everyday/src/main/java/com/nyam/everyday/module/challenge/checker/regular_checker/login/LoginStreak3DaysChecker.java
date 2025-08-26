package com.nyam.everyday.module.challenge.checker.regular_checker.login;

import com.nyam.everyday.module.challenge.checker.AbstractCountBasedChecker;
import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.challenge.repository.ChallengeRepository;
import com.nyam.everyday.module.member.entity.Member;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class LoginStreak3DaysChecker extends AbstractCountBasedChecker {

  protected LoginStreak3DaysChecker(
      ChallengeRepository challengeRepository,
      ChallengeCheckService challengeCheckService,
      ApplicationEventPublisher publisher) {
    super(challengeRepository, challengeCheckService, publisher);
  }

  @Override
  public ChallengeCode getChallengeCode() {
    return ChallengeCode.LOGIN_STREAK_3DAYS;
  }

  @Override
  public ChallengeTag getChallengeTag() {
    return ChallengeTag.LOGIN;
  }

  @Override
  public long getProgress(Member member, Challenge challenge) {
    // Member의 연속 기록을 그대로 반환
    return member.getConsecutiveLoginDays();
  }
}
