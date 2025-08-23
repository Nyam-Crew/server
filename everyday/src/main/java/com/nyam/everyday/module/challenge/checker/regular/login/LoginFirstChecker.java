package com.nyam.everyday.module.challenge.checker.regular.login;

import com.nyam.everyday.module.challenge.checker.ChallengeChecker;
import com.nyam.everyday.module.challenge.checker.event.event.MCDCreateEvent;
import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCheckType;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.member.entity.Member;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginFirstChecker implements ChallengeChecker {

  private final ChallengeCheckService challengeCheckService;
  private final ApplicationEventPublisher publisher;

  @Override
  public ChallengeCode getChallengeCode() {
    return ChallengeCode.LOGIN_FIRST;
  }

  @Override
  public ChallengeTag getChallengeTag() {
    return ChallengeTag.LOGIN;
  }

  @Override
  public ChallengeCheckType getChallengeCheckType() {
    return ChallengeCheckType.BY_DAY;
  }

  @Override
  public void check(Member member, LocalDate targetDate) {
    log.info("First Login Checker 실행");

    // 챌린지 정보 불러오기
    Challenge challenge = challengeCheckService.getChallengeByChallengeCode(this.getChallengeCode());

    // 이미 달성했으면 체크 안 함
    if (!challengeCheckService.needToCheckChallenge(member, challenge)) return;

    // 이 사람이 오늘 잘 로그인했는가?
    if (member.getLastLoginDate().toLocalDate().equals(LocalDate.now())) {
      // MCD 생성 이벤트 발행
      publisher.publishEvent(new MCDCreateEvent(member, challenge, targetDate));
    }
  }

  @Override
  public Long getProgress(Member member) {
    return 0L;
  }

  @Override
  public Boolean isSatisfied(Integer progressCount) {
    // 한번이라도 로그인했으면 달성
    return progressCount > 0;
  }
}
