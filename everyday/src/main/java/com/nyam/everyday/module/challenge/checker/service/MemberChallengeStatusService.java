package com.nyam.everyday.module.challenge.checker.service;

import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.MemberChallengeStatus;
import com.nyam.everyday.module.challenge.repository.MemberChallengeStatusRepository;
import com.nyam.everyday.module.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberChallengeStatusService {

  private final MemberChallengeStatusRepository memberChallengeStatusRepository;

  /**
   * 회원과 챌린지에 대응하는 {@link MemberChallengeStatus}를 조회합니다. 없으면 새로 생성하여 저장한 뒤 반환합니다.
   *
   * @param member    회원
   * @param challenge 챌린지
   * @return 조회 또는 생성된 {@link MemberChallengeStatus}
   */
  public MemberChallengeStatus getOrCreateMCS(Member member, Challenge challenge) {
    MemberChallengeStatus memberChallengeStatus = memberChallengeStatusRepository.getMCSByMemberChallenge(
        member.getMemberId(), challenge.getId()).orElse(null);

    // 값이 있으면 그대로 반환, 없으면 만들어서 저장하고 반환
    if (memberChallengeStatus == null) {
      memberChallengeStatus = memberChallengeStatusRepository.save(MemberChallengeStatus.builder()
          .member(member)
          .challenge(challenge)
          .build()
      );
    }

    return memberChallengeStatus;
  }
}
