package com.nyam.everyday.module.challenge.checker;

import com.nyam.everyday.module.challenge.entity.ChallengeType;
import com.nyam.everyday.module.member.entity.Member;


// 체크를 위해 필요한 기능들이 포함된 체커
public interface ChallengeChecker {

  // 타입 반환
  ChallengeType getSupportedType();

  Boolean check(Member member);
}
