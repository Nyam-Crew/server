package com.nyam.everyday.module.challenge.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.MemberChallengeStatus;
import com.nyam.everyday.module.challenge.repository.MemberChallengeStatusRepository;
import com.nyam.everyday.module.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
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

  @Transactional(readOnly = true)
  // 어떤 유저가 특정 챌린지를 완료했는지 체크한다.
  public boolean getIsCleared(Long memberId, Long challengeId) {
    return memberChallengeStatusRepository.getIsCleared(memberId, challengeId).orElse(false);
  }

  @Transactional(readOnly = true)
  // 어떤 유저의 챌린지 진행도를 체크한다
  public Long getProgressCount(Long memberId, Long challengeId) {
    return memberChallengeStatusRepository.getProgressCount(memberId, challengeId).orElse(0L);
  }

  @Transactional(readOnly = true)
  // 특정 유저가 챌린지에 참여중인지 체크한다
  public boolean isAttending(Long memberId, Long challengeId) {
    MemberChallengeStatus mcs = memberChallengeStatusRepository.getMCSByMemberChallenge(memberId, challengeId).orElse(null);
    return mcs != null;
  }

  // 특정 유저를 챌린지에 참여시킨다 (MCS 추가)
  public void attendToEventChallenge(Member member, Challenge challenge) {
    MemberChallengeStatus mcs = MemberChallengeStatus.builder()
        .member(member)
        .challenge(challenge)
        .build();

    // 이미 있는 값인지 한번 조회
    if (memberChallengeStatusRepository.getMCSByMemberChallenge(member.getMemberId(),
        challenge.getId()).isPresent()) {
      throw BaseException.MCS_ALREADY_EXISTS;
    }

    memberChallengeStatusRepository.save(mcs);
  }
}
