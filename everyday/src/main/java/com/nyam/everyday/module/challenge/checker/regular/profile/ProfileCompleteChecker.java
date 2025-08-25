package com.nyam.everyday.module.challenge.checker.regular.profile;

import com.nyam.everyday.module.challenge.checker.ChallengeChecker;
import com.nyam.everyday.module.challenge.checker.event.event.ProgressRecomputeEvent;
import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCheckType;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.challenge.repository.ChallengeRepository;
import com.nyam.everyday.module.member.entity.Gender;
import com.nyam.everyday.module.member.entity.Member;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class ProfileCompleteChecker implements ChallengeChecker {

  private final ChallengeCheckService challengeCheckService;
  private final ChallengeRepository challengeRepository;
  private final ApplicationEventPublisher publisher;

  @Override
  public ChallengeCode getChallengeCode() {
    return ChallengeCode.PROFILE_COMPLETE;
  }

  @Override
  public ChallengeTag getChallengeTag() {
    return ChallengeTag.PROFILE;
  }

  @Override
  public ChallengeCheckType getChallengeCheckType() {
    return ChallengeCheckType.BY_COUNT;
  }

  @Override
  public void check(Member member, LocalDate targetDate) {
    Challenge challenge = challengeRepository.getByChallengeCode(getChallengeCode());

    if (!challengeCheckService.needToCheckChallenge(member, challenge)) {
      return;
    }

    publisher.publishEvent(new ProgressRecomputeEvent(member, challenge));
  }

  @Override
  public Long getProgress(Member member) {
    // 멤버가 모든 정보를 다 채웠어야 OK가 된다.
    if (member.getGender() == Gender.U) return 0L;
    if (member.getWeight().equals(BigDecimal.valueOf(0L))) return 0L;
    if (member.getAge() == 0) return 0L;
    if (member.getTargetWeight().equals(BigDecimal.valueOf(0L))) return 0L;

    return 1L;
  }

  @Override
  public Boolean isSatisfied(Integer progressCount) {
    return progressCount > 0;
  }
}
