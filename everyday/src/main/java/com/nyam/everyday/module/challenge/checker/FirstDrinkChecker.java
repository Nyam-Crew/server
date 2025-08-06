package com.nyam.everyday.module.challenge.checker;

import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeTitle;
import com.nyam.everyday.module.challenge.entity.ChallengeType;
import com.nyam.everyday.module.challenge.repository.ChallengeRepository;
import com.nyam.everyday.module.challenge.repository.MemberChallengeStatusRepository;
import com.nyam.everyday.module.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class FirstDrinkChecker extends AbstractChallengeChecker {

  // 상속구조 구현을 위한 생성자
  public FirstDrinkChecker(MemberChallengeStatusRepository memberChallengeRepository,
      ChallengeRepository challengeRepository) {
    super(memberChallengeRepository);
    this.challengeRepository = challengeRepository;
  }

  private final ChallengeRepository challengeRepository;

  @Override
  public ChallengeType getSupportedType() {
    return ChallengeType.WATER;
  }

  @Override
  public Boolean check(Member member) {
    Challenge challenge = challengeRepository.getChallengeByChallengeTitle(
        ChallengeTitle.FIRST_DRINK);

    // 챌린지 체크할 필요 없으면, false 반환
    if (!needToCheckChallenge(member, challenge)) {
      return false;
    }

    // 달성 여부 확인
    ///  달성 여부 확인을 위한 로직 구현
    return false;
  }
}
