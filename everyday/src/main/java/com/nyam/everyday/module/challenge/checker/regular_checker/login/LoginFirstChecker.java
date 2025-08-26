package com.nyam.everyday.module.challenge.checker.regular_checker.login;

import com.nyam.everyday.module.challenge.checker.AbstractDateBasedChecker;
import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.challenge.repository.ChallengeRepository;
import com.nyam.everyday.module.challenge.repository.MemberChallengeDayRepository;
import com.nyam.everyday.module.member.entity.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class LoginFirstChecker extends AbstractDateBasedChecker {

  protected LoginFirstChecker(
          ChallengeRepository challengeRepository,
          ChallengeCheckService challengeCheckService,
          MemberChallengeDayRepository memberChallengeDayRepository,
          ApplicationEventPublisher publisher
  ) {
    super(challengeRepository, challengeCheckService, memberChallengeDayRepository, publisher);
  }

  @Override
  public ChallengeCode getChallengeCode() {
    return ChallengeCode.LOGIN_FIRST;
  }

  @Override
  public ChallengeTag getChallengeTag() {
    return ChallengeTag.LOGIN;
  }

  @Override
  protected void doCheckAndPublish(Member member, Challenge challenge, LocalDate targetDate) {
    // 이 사람이 잘 로그인했는가?
    if (member.getLastLoginDate().toLocalDate().equals(LocalDate.now())) {
      // MCD 생성 이벤트 발행
      publishMCDCreateEvent(member, challenge, targetDate);
    }
  }
}
