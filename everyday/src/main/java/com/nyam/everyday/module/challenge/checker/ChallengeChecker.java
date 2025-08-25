package com.nyam.everyday.module.challenge.checker;

import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCheckType;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.member.entity.Member;
import java.time.LocalDate;


// 체크를 위해 필요한 기능들이 포함된 체커
// 챌린지에는 날짜 기반(3일 이상 출석), 횟수 기반 챌린지(게시판에 글 5회 이상 작성)가 있다
// 각자가 다른 루트로 체크되어야 하기에, 각각에 필요한 함수 원형을 구현해두었음.
public interface ChallengeChecker {

  // 챌린지코드와 챌린지를 매핑하기 위해 사용
  ChallengeCode getChallengeCode();

  // 타입 반환 함수 (어떤 태그의 챌린지인지 확인하기 위함)
  // ChallengeRegistry 에서 체커의 Map을 구현할 때 사용된다.
  ChallengeTag getChallengeTag();

  // 임계치 이상이면 True 반환할 것
  boolean isSatisfied(long progressCount);

  // ChallengeCheckListener가 공통적으로 호출하는 Entry Point
  void check(Member member, LocalDate targetDate);

  // 각 챌린지의 진행도를 체크
  long getProgress(Member member, Challenge challenge);
}
