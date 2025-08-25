// file: com/nyam/everyday/module/challenge/checker/registry/ChallengeCheckerRegistry.java
package com.nyam.everyday.module.challenge.checker.registry;

import com.nyam.everyday.module.challenge.checker.ChallengeChecker;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChallengeCheckerRegistry {

  // 모든 ChallengeChecker 구현체들을 Spring이 자동으로 주입합니다.
  // 예: FirstDrinkChecker, FiveDaysWaterChecker 등
  private final List<ChallengeChecker> allCheckers;


  // ChallengeType 별로 어떤 Checker들이 있는지 분류해 저장하는 Map
  private Map<ChallengeTag, List<ChallengeChecker>> checkerMap;
  // Checker를 Code 기반으로 매핑
  private Map<ChallengeCode, ChallengeChecker> checkerCodeMap;

  /**
   * 애플리케이션 기동 시 한 번 실행되어 각 {@link ChallengeChecker}를 담당 {@link ChallengeTag} 기준으로
   * {@code checkerMap}에 분류해 저장합니다.
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

    checkerCodeMap = new HashMap<>();
    // 모든 ChallengeChecker들을 순회하며, ChallengeCode별로 어떤 체커가 사용되는지 분류합니다.
    for (ChallengeChecker checker : allCheckers) {
      ChallengeCode code = checker.getChallengeCode();

      checkerCodeMap.put(code, checker);
    }
  }

  public List<ChallengeChecker> getCheckers(ChallengeTag tag) {
    return checkerMap.getOrDefault(tag, null);
  }

  public ChallengeChecker getChecker(Challenge challenge) {
    return checkerCodeMap.get(challenge.getChallengeCode());
  }
}
