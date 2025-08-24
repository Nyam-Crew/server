package com.nyam.everyday.module.challenge.checker.service;

import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.MemberChallengeDay;
import com.nyam.everyday.module.challenge.repository.ChallengeRepository;
import com.nyam.everyday.module.challenge.repository.MemberChallengeDayRepository;
import com.nyam.everyday.module.challenge.repository.MemberChallengeStatusRepository;
import com.nyam.everyday.module.member.entity.Member;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChallengeCheckService {

  private final ChallengeRepository challengeRepository;
  private final MemberChallengeStatusRepository memberChallengeStatusRepository;
  private final MemberChallengeDayRepository memberChallengeDayRepository;


  /**
   * 특정 챌린지를 현재 시점에 검사할 필요가 있는지 여부를 판단합니다. - 이미 클리어된 경우: {@code false} - 기간 미설정 챌린지: {@code true} -
   * 기간 설정 챌린지: 오늘 날짜가 시작일과 종료일 범위 내면 {@code true}
   *
   * @param member    회원
   * @param challenge 대상 챌린지
   * @return 검사 필요 여부
   */
  @Transactional(readOnly = true)
  public boolean needToCheckChallenge(Member member, Challenge challenge) {
    Long memberId = member.getMemberId();
    Long challengeId = challenge.getId();

    Boolean isCleared = memberChallengeStatusRepository.getIsCleared(memberId, challengeId)
        .orElse(false);

    // 이미 달성할 챌린지라면 확인 필요 없음
    if (isCleared) {
      return false;
    }

    // 달성 기간 없는 챌린지라면 달성 가능
    if (challenge.getStartDate() == null) {
      return true;
    }

    // 챌린지 진행 기간 체크
    LocalDate startDate = challenge.getStartDate().toLocalDate();
    LocalDate endDate = challenge.getEndDate().toLocalDate();
    LocalDate today = LocalDate.now();

    // 챌린지 진행 기간에 해당하면 체크해야 함
    return !today.isBefore(startDate) && !today.isAfter(endDate);
  }

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

  /**
   * 챌린지 태그에 해당하는 Challnge 정보를 불러옵니다.
   *
   * @param challengeCode 찾고자 하는 챌린지 코드
   * @return 조회된 Challenge 정보
   */
  public Challenge getChallengeByChallengeCode(ChallengeCode challengeCode) {
    return challengeRepository.getByChallengeCode(challengeCode);
  }
}