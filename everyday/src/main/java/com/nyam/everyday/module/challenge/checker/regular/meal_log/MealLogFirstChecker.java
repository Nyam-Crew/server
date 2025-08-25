package com.nyam.everyday.module.challenge.checker.regular.meal_log;

import com.nyam.everyday.module.challenge.checker.ChallengeChecker;
import com.nyam.everyday.module.challenge.checker.event.event.ProgressRecomputeEvent;
import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCheckType;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.challenge.repository.ChallengeRepository;
import com.nyam.everyday.module.meal.repository.MealLogRepository;
import com.nyam.everyday.module.member.entity.Member;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MealLogFirstChecker implements ChallengeChecker {

  private final ChallengeRepository challengeRepository;
  private final ChallengeCheckService challengeCheckService;
  private final ApplicationEventPublisher publisher;
  private final MealLogRepository mealLogRepository;

  @Override
  public ChallengeCode getChallengeCode() {
    return ChallengeCode.MEAL_LOG_FIRST;
  }

  @Override
  public ChallengeTag getChallengeTag() {
    return ChallengeTag.MEAL_LOG;
  }

  @Override
  public ChallengeCheckType getChallengeCheckType() {
    return ChallengeCheckType.BY_COUNT;
  }

  @Override
  public void check(Member member, LocalDate targetDate) {
    Challenge challenge = challengeRepository.getByChallengeCode(getChallengeCode());

    if (!challengeCheckService.needToCheckChallenge(member, challenge)) return;

    publisher.publishEvent(new ProgressRecomputeEvent(member, challenge));
  }

  @Override
  public Long getProgress(Member member) {
    // 식단 기록 갯수 세기
    return mealLogRepository.getCountByMemberId(member.getMemberId());
  }

  @Override
  public Boolean isSatisfied(Integer progressCount) {
    return progressCount > 0;
  }
}
