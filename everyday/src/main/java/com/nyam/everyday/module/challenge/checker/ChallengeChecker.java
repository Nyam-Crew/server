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
  ChallengeTag getChallengeTag();

  // 날짜 기반 챌린지인지, 횟수 기반 챌린지인지 구분
  ChallengeCheckType getChallengeCheckType();

  // 이미 달성한 챌린지인지, 날짜 기반 챌린지라면 그 날짜에 조건을 달성했는지까지 체크
  void check(Member member, LocalDate targetDate);

  // 횟수 기반 챌린지라면, 횟수 계산 로직을 넣는다.
  // 날짝 기반 챌린지라면, 더미함수이기에 아무 값이나 반환하도록 하면 됨(사용되지 않음)
  Long getProgress(Member member);

  // progressCount번 조건을 달성했을 때 챌린지 달성 조건에 해당하면 true, 아니면 false 반환
  Boolean isSatisfied(Integer progressCount);
}
