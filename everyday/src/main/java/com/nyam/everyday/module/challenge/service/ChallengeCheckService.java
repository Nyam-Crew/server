package com.nyam.everyday.module.challenge.service;

import com.nyam.everyday.module.challenge.checker.ChallengeChecker;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.challenge.entity.ChallengeType;
import com.nyam.everyday.module.challenge.entity.MemberChallengeStatus;
import com.nyam.everyday.module.challenge.repository.ChallengeRepository;
import com.nyam.everyday.module.challenge.repository.MemberChallengeStatusRepository;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.notification.service.NotifyToReactService;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChallengeCheckService {

  private final ChallengeRepository challengeRepository;
  private final MemberChallengeStatusRepository memberChallengeStatusRepository;
  private final NotifyToReactService notifyToReactService;

  // 모든 ChallengeChecker 구현체들을 Spring이 자동으로 주입합니다.
  // 예: FirstDrinkChecker, FiveDaysWaterChecker 등
  private final List<ChallengeChecker> allCheckers;


  // ChallengeType 별로 어떤 Checker들이 있는지 분류해 저장하는 Map
  private Map<ChallengeTag, List<ChallengeChecker>> checkerMap;

  /**
   * 애플리케이션 기동 시 한 번 실행되어 각 {@link ChallengeChecker}를
   * 담당 {@link ChallengeTag} 기준으로 {@code checkerMap}에 분류해 저장합니다.
   */
  @PostConstruct // Spring이 빈을 초기화한 직후 실행되는 메서드입니다.
  public void init() {
    checkerMap = new HashMap<>();
    // 모든 ChallengeChecker들을 순회하며, 지원하는 ChallengeType별로 분류합니다.
    for (ChallengeChecker checker : allCheckers) {
      // 각 Checker가 담당하는 ChallengeType을 조회합니다.
      ChallengeTag tag = checker.getChallengeTag();
      // 해당 type에 맞는 리스트가 없으면 새로 생성한 후, Checker를 추가합니다.
      checkerMap.computeIfAbsent(tag, k -> new ArrayList<>()).add(checker);
    }
  }

  /**
   * 주어진 태그에 해당하는 모든 {@link ChallengeChecker}를 실행합니다.
   * 태그에 매핑된 검사기가 없으면 아무 작업도 수행하지 않습니다.
   *
   * @param member 검사 대상 회원
   * @param tag 사용자 행동에 해당하는 태그
   */
  public void checkChallenges(Member member, ChallengeTag tag) {
    // 해당 타입의 챌린지 검사기 리스트를 조회합니다.
    List<ChallengeChecker> checkers = checkerMap.get(tag);
    // 만약 검사기가 없다면 아무것도 하지 않고 종료합니다.
    if (checkers == null) {
      return;
    }

    // 타입에 해당하는 모든 ChallengeChecker를 실행합니다.
    for (ChallengeChecker checker : checkers) {
      // 각 챌린지에 대해 검사 수행
      checker.check(member);
      // 각 Checker 내부에서 조건을 만족하면 챌린지 달성 처리 등이 이뤄집니다.
    }
  }

  /**
   * 특정 챌린지를 현재 시점에 검사할 필요가 있는지 여부를 판단합니다.
   * - 이미 클리어된 경우: {@code false}
   * - 기간 미설정 챌린지: {@code true}
   * - 기간 설정 챌린지: 오늘 날짜가 시작일과 종료일 범위 내면 {@code true}
   *
   * @param member 회원
   * @param challenge 대상 챌린지
   * @return 검사 필요 여부
   */
  @Transactional(readOnly = true)
  public boolean needToCheckChallenge(Member member, Challenge challenge) {
    Long memberId = member.getMemberId();
    Long challengeId = challenge.getId();

    Boolean isCleared = memberChallengeStatusRepository.getIsCleared(memberId, challengeId).orElse(false);

    // 이미 달성할 챌린지라면 확인 필요 없음
    if (isCleared) {
      return false;
    }

    // 달성 기간 없는 챌린지라면 달성 가능
    if (challenge.getStartDate() == null) return true;

    // 챌린지 진행 기간 체크
    LocalDate startDate = challenge.getStartDate().toLocalDate();
    LocalDate endDate = challenge.getEndDate().toLocalDate();
    LocalDate today = LocalDate.now();

    // 챌린지 진행 기간에 해당하면 체크해야 함
    return !today.isBefore(startDate) && !today.isAfter(endDate);
  }

  /**
   * 일일 미션 진행도를 1 증가시키고 증가된 진행도를 반환합니다.
   * 대응하는 {@link MemberChallengeStatus}가 없으면 생성합니다.
   *
   * @param member 회원
   * @param challenge 대상 챌린지
   * @return 증가 이후 진행도
   */
  public Integer addProgressCount(Member member, Challenge challenge) {
    MemberChallengeStatus memberChallengeStatus = getMCS(member, challenge);
    memberChallengeStatus.addProgressCount();

    return memberChallengeStatus.getProgressCount();
  }

  /**
   * 챌린지를 클리어 상태로 설정합니다.
   * 대응하는 {@link MemberChallengeStatus}가 없으면 생성합니다.
   *
   * @param member 회원
   * @param challenge 대상 챌린지
   */
  public void completeChallenge(Member member, Challenge challenge) {
    MemberChallengeStatus memberChallengeStatus = getMCS(member, challenge);
    memberChallengeStatus.setAsCleared();
  }

  /**
   * 회원과 챌린지에 대응하는 {@link MemberChallengeStatus}를 조회합니다.
   * 없으면 새로 생성하여 저장한 뒤 반환합니다.
   *
   * @param member 회원
   * @param challenge 챌린지
   * @return 조회 또는 생성된 {@link MemberChallengeStatus}
   */
  private MemberChallengeStatus getMCS(Member member, Challenge challenge) {
    MemberChallengeStatus memberChallengeStatus = memberChallengeStatusRepository.getMCSByMemberChallenge(member.getMemberId(), challenge.getId()).orElse(null);

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

  /**
   * 챌린지 태그에 해당하는 Challnge 정보를 불러옵니다.
   *
   * @param challengeCode 찾고자 하는 챌린지 코드
   * @return 조회된 Challenge 정보
   */
  public Challenge getChallengeByChallengeCode(ChallengeCode challengeCode) {
    return challengeRepository.findByChallengeCode(challengeCode);
  }
}