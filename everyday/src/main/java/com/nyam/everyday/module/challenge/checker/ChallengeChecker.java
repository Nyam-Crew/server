package com.nyam.everyday.module.challenge.checker;

import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.member.entity.Member;
import java.time.LocalDate;


// 체크를 위해 필요한 기능들이 포함된 체커
public interface ChallengeChecker {
  ChallengeCode getChallengeCode();

  // 타입 반환 함수 (어떤 태그의 챌린지인지 확인하기 위함)
  ChallengeTag getChallengeTag();

  // 어떤 날짜에 챌린지를 달성했는지 체크한다.
  void check(Member member, LocalDate targetDate);

  // progressCount번 조건을 달성했을 때 챌린지 달성 조건에 해당하면 true, 아니면 false 반환
  Boolean isSatisfied(Integer progressCount);
}
