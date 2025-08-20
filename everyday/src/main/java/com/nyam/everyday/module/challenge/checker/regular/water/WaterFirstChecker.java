package com.nyam.everyday.module.challenge.checker.regular.water;

import com.nyam.everyday.module.challenge.checker.ChallengeChecker;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.challenge.repository.ChallengeRepository;
import com.nyam.everyday.module.challenge.service.ChallengeCheckService;
import com.nyam.everyday.module.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WaterFirstChecker implements ChallengeChecker {

  private final ChallengeCheckService challengeCheckService;
  private final ChallengeRepository challengeRepository;

  @Override
  public ChallengeTag getChallengeTag() {
    return ChallengeTag.WATER;
  }

  @Override
  public Boolean check(Member member) {

    Challenge challenge = challengeRepository.findByChallengeCode(ChallengeCode.WATER_FIRST);

    // 챌린지 체크할 필요 없으면, false 반환
    if (!challengeCheckService.needToCheckChallenge(member, challenge)) {
      return false;
    }

    // 달성 여부 확인
    /// 처음 물 마신것이니, 바로 달성시킨다
    challengeCheckService.completeChallenge(member, challenge);

    return true;
  }
}