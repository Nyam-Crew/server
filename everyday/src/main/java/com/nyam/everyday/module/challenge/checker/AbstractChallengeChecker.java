package com.nyam.everyday.module.challenge.checker;


import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.repository.MemberChallengeRepository;
import com.nyam.everyday.module.member.entity.Member;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;

// 챌린지체커에 기본 기능을 제공해주기 위한 추상 클래스
@RequiredArgsConstructor
public abstract class AbstractChallengeChecker implements ChallengeChecker {

  protected final MemberChallengeRepository memberChallengeRepository;

  // 해당 챌린지의 달성 여부 확인이 필요한지 확인하는 함수
  protected boolean needToCheckChallenge(Member member, Challenge challenge) {
    Long memberId = member.getMemberId();
    Long challengeId = challenge.getChallengeId();

    // 이미 달성할 챌린지라면 확인 필요 없음
    if (memberChallengeRepository.existsByMember_MemberIdAndChallenge_ChallengeId(memberId,
        challengeId)) {
      return false;
    }

    LocalDate startDate = challenge.getChallengeStartDate().toLocalDate();
    LocalDate endDate = challenge.getChallengeEndDate().toLocalDate();
    LocalDate today = LocalDate.now();
    // 챌린지 진행 기간에 해당하면 체크해야 함
    return !today.isBefore(startDate) && !today.isAfter(endDate);
  }
}

