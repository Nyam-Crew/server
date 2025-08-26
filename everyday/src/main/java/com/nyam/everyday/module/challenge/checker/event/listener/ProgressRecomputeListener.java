package com.nyam.everyday.module.challenge.checker.event.listener;

import com.nyam.everyday.module.challenge.checker.ChallengeChecker;
import com.nyam.everyday.module.challenge.checker.event.event.ChallengeClearedEvent;
import com.nyam.everyday.module.challenge.checker.event.event.ProgressRecomputeEvent;
import com.nyam.everyday.module.challenge.checker.registry.ChallengeCheckerRegistry;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.MemberChallengeStatus;
import com.nyam.everyday.module.challenge.service.MemberChallengeStatusService;
import com.nyam.everyday.module.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;


/**
 * 어떤 챌린지의 조건을 만족해 챌린지 완성도를 다시 확인할 필요가 있을 때 발행되는 이벤트를 처리하는 리스너입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProgressRecomputeListener {

  private final ChallengeCheckerRegistry challengeCheckerRegistry;
  private final MemberChallengeStatusService memberChallengeStatusService;
  private final ApplicationEventPublisher publisher;

  @Async("challengeExecutor")
  @Transactional(propagation = REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void progressRecompute(ProgressRecomputeEvent evt) {

    Member member = evt.getMember();
    Challenge challenge = evt.getChallenge();

//    log.info("{} ProgressRecompute 동작", challenge.getTitle());

    // 1) 챌린지 기반으로 체커 가져오기
    ChallengeChecker checker = challengeCheckerRegistry.getChecker(challenge);

    // 2) 체커 기반으로 진행도 조회
    long progressCount = checker.getProgress(member, challenge);

    // 3) 진행도 update
    MemberChallengeStatus mcs = memberChallengeStatusService.getOrCreateMCS(member, challenge);
    mcs.setProgressCount(progressCount);

    // 4) 조건 달성 시에, 달성 이벤트 발행
    if (progressCount >= challenge.getTargetCount()) {
      publisher.publishEvent(new ChallengeClearedEvent(member, challenge));
    }
  }
}
