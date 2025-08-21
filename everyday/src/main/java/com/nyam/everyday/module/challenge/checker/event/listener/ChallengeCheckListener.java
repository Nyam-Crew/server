package com.nyam.everyday.module.challenge.checker.event.listener;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.module.challenge.checker.ChallengeChecker;
import com.nyam.everyday.module.challenge.checker.event.event.ChallengeCheckEvent;
import com.nyam.everyday.module.challenge.checker.registry.ChallengeCheckerRegistry;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeCheckListener {

  private final MemberRepository memberRepository;
  private final ChallengeCheckerRegistry challengeCheckerRegistry;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onChallengeCheck(ChallengeCheckEvent challengeCheckEvent) {
    log.info("챌린지체크 리스너 동작");
    Long memberId = challengeCheckEvent.getMemberId();
    ChallengeTag tag = challengeCheckEvent.getChallengeTag();
    LocalDate targetDate = challengeCheckEvent.getTargetDate();

    // 멤버 정보 가져오기
    Member member = memberRepository.findByMemberId(memberId)
        .orElseThrow(() -> BaseException.MEMBER_NOT_FOUND);

    // 해당 타입의 챌린지 검사기 리스트를 조회합니다.
    List<ChallengeChecker> checkers = challengeCheckerRegistry.getCheckers(tag);

    // 만약 검사기가 없다면 아무것도 하지 않고 종료합니다.
    if (checkers == null) {
      return;
    }

    // 타입에 해당하는 모든 ChallengeChecker를 실행합니다.
    for (ChallengeChecker checker : checkers) {
      // 각 챌린지에 대해 검사 수행
      checker.check(member, targetDate);
      // 각 Checker 내부에서 조건을 만족하면 MCDCreateEvent를 발행합니다.
    }
  }
}