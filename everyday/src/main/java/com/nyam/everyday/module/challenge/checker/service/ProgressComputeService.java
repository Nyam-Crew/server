package com.nyam.everyday.module.challenge.checker.service;

import com.nyam.everyday.module.challenge.checker.ChallengeChecker;
import com.nyam.everyday.module.challenge.checker.registry.ChallengeCheckerRegistry;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.MemberChallengeStatus;
import com.nyam.everyday.module.challenge.repository.MemberChallengeDayRepository;
import com.nyam.everyday.module.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgressComputeService {

  private final MemberChallengeDayRepository memberChallengeDayRepository;
  private final ChallengeCheckerRegistry challengeCheckerRegistry;
  private final MemberChallengeStatusService memberChallengeStatusService;


  /**
   * 유저, 챌린지에 해당하는 MemberChallengeDay의 갯수를 세서 member_chllenge_status의 progress_Count를 변경합니다.
   *
   * @param member    해당하는 멤버
   * @param challenge 해당하는 챌린지
   * @return 변경된 progressCount 값
   */
  public Integer computeProgressCountByDay(Member member, Challenge challenge) {
    Long memberId = member.getMemberId();
    Long challengeId = challenge.getId();

    // 멤버가 특정 챌린지를 깬 날짜가 총 몇일인지 확인한다.
    Integer progressCount = memberChallengeDayRepository.countMemberChallengeDay(memberId,
        challengeId);

    // 이를 기반으로 memberChallengeStatus를 갱신한다
    MemberChallengeStatus mcs = memberChallengeStatusService.getOrCreateMCS(member, challenge);
    mcs.setProgressCount(progressCount);

    return progressCount;
  }

  public Integer computeProgressCountByCount(Member member, Challenge challenge) {
    ChallengeChecker checker = challengeCheckerRegistry.getChecker(challenge);

    // 체커에 정의된 recompute 함수를 사용하고, mcs를 갱신한다.
    Long progressCount = checker.getProgress(member);

    // 이를 기반으로 memberChallengeStatus를 갱신한다
    MemberChallengeStatus mcs = memberChallengeStatusService.getOrCreateMCS(member, challenge);
    mcs.setProgressCount(progressCount.intValue());

    return progressCount.intValue();
  }
}
