package com.nyam.everyday.module.challenge.checker;

import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.challenge.entity.ChallengeType;
import com.nyam.everyday.module.member.entity.Member;


// 체크를 위해 필요한 기능들이 포함된 체커
public interface ChallengeChecker {

  // 타입 반환 함수 (어떤 태그의 챌린지인지 확인하기 위함)
  ChallengeTag getChallengeTag();

  // 챌린지 달성 여부를 체크하는 함수
  Boolean check(Member member);
}
