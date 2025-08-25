package com.nyam.everyday.module.challenge.checker.regular_checker.meal_log;

import com.nyam.everyday.module.challenge.checker.AbstractCountBasedChecker;
import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.challenge.repository.ChallengeRepository;
import com.nyam.everyday.module.meal.repository.MealLogRepository;
import com.nyam.everyday.module.member.entity.Member;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class MealLogFirstChecker extends AbstractCountBasedChecker {

  private final MealLogRepository mealLogRepository;

  protected MealLogFirstChecker(
          ChallengeRepository challengeRepository,
          ChallengeCheckService challengeCheckService,
          ApplicationEventPublisher publisher,
          MealLogRepository mealLogRepository
  ) {
    super(1, challengeRepository, challengeCheckService, publisher);
    this.mealLogRepository = mealLogRepository;
  }

  @Override
  public ChallengeCode getChallengeCode() {
    return ChallengeCode.MEAL_LOG_FIRST;
  }

  @Override
  public ChallengeTag getChallengeTag() {
    return ChallengeTag.MEAL_LOG;
  }

  @Override
  public long getProgress(Member member, Challenge challenge) {
    // 식단 기록 갯수 세서 반환
    return mealLogRepository.getCountByMemberId(member.getMemberId());
  }
}
