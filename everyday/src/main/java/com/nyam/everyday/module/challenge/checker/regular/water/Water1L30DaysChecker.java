package com.nyam.everyday.module.challenge.checker.regular.water;

import com.nyam.everyday.module.challenge.checker.ChallengeChecker;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.challenge.service.ChallengeCheckService;
import com.nyam.everyday.module.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Water1L30DaysChecker implements ChallengeChecker {

  private final ChallengeCheckService challengeCheckService;
  private final

  @Override
  public ChallengeTag getChallengeTag() {
    return ChallengeTag.WATER;
  }

  @Override
  public Boolean check(Member member) {
    // 챌린지 정보 가져오기
    Challenge water1L30DaysChallenge = challengeCheckService.getChallengeByChallengeCode(
        ChallengeCode.WATER_1L_30DAYS);

    // 달성 조건 확인

  }
}
