package com.nyam.everyday.module.challenge.checker.regular_checker.login;

import com.nyam.everyday.module.challenge.checker.AbstractDateBasedChecker;
import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.challenge.repository.ChallengeRepository;
import com.nyam.everyday.module.challenge.repository.MemberChallengeDayRepository;
import com.nyam.everyday.module.member.entity.Member;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class Login30DaysChecker extends AbstractDateBasedChecker {

  protected Login30DaysChecker(
          MemberChallengeDayRepository memberChallengeDayRepository,
          ApplicationEventPublisher publisher,
          ChallengeRepository challengeRepository,
          ChallengeCheckService challengeCheckService
  ) {
    super(challengeRepository, challengeCheckService, memberChallengeDayRepository, publisher);
  }

  @Override
  public ChallengeCode getChallengeCode() {
    return ChallengeCode.LOGIN_30DAYS;
  }

  @Override
  public ChallengeTag getChallengeTag() {
    return ChallengeTag.LOGIN;
  }

  @Override
  protected void doCheckAndPublish(Member member, Challenge challenge, LocalDate targetDate) {
    // 이 사람이 오늘 잘 로그인했으면, MCD 추가
    if (member.getLastLoginDate().toLocalDate().equals(LocalDate.now())) {
      publishMCDCreateEvent(member, challenge,  targetDate);
    }
  }
}
