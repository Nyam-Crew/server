package com.nyam.everyday.module.challenge.service;

import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.MemberChallengeDay;
import com.nyam.everyday.module.challenge.repository.MemberChallengeDayRepository;
import com.nyam.everyday.module.member.entity.Member;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberChallengeDayService {

  private final MemberChallengeDayRepository memberChallengeDayRepository;

  /**
   * 멤버, 챌린지, 날짜에 적절한 MemberChallengeDay 데이터를 생성합니다, 이미 같은 값이 존재한다면, 수행하지 않습니다.
   *
   * @param member    회원
   * @param challenge 대상 챌린지
   * @param date      챌린지 진행 날짜
   */
  public void addMemberChallengeDay(Member member, Challenge challenge, LocalDate date) {
    if (!memberChallengeDayRepository.existsByMember_MemberIdAndChallenge_IdAndTargetDate(
        member.getMemberId(), challenge.getId(), date)) {
      memberChallengeDayRepository.save(MemberChallengeDay.builder()
          .member(member)
          .challenge(challenge)
          .targetDate(date)
          .build()
      );
    }
  }

  /**
   * 멤버, 챌린지, 날짜에 적절한 MemberChallengeDay 데이터를 삭제합니다(진행 취소 처리) 데이터가 존재하지 않는다면, 수행하지 않습니다.
   *
   * @param member    회원
   * @param challenge 대상 챌린지
   * @param date      챌린지 진행 날짜
   */
  public void deleteMemberChallengeDay(Member member, Challenge challenge, LocalDate date) {
    if (memberChallengeDayRepository.existsByMember_MemberIdAndChallenge_IdAndTargetDate(
        member.getMemberId(), challenge.getId(), date)) {
      memberChallengeDayRepository.deleteMemberChallengeDay(member.getMemberId(), challenge.getId(),
          date);
    }
  }
}
