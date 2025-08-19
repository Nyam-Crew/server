package com.nyam.everyday.module.challenge.service;

import com.nyam.everyday.module.challenge.checker.ChallengeChecker;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.challenge.entity.ChallengeType;
import com.nyam.everyday.module.member.entity.Member;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service // 이 클래스는 Spring의 Service 빈으로 등록되어 비즈니스 로직을 담당합니다.
@RequiredArgsConstructor // final 필드에 대해 생성자를 자동 생성합니다.
public class ChallengeCheckService {

  // 모든 ChallengeChecker 구현체들을 Spring이 자동으로 주입합니다.
  // 예: FirstDrinkChecker, FiveDaysWaterChecker 등
  private final List<ChallengeChecker> allCheckers;

  // ChallengeType 별로 어떤 Checker들이 있는지 분류해 저장하는 Map
  private Map<ChallengeTag, List<ChallengeChecker>> checkerMap;

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

  // 사용자가 어떤 행동을 했을 때, 관련된 ChallengeType을 기준으로 챌린지를 검사합니다.
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
}