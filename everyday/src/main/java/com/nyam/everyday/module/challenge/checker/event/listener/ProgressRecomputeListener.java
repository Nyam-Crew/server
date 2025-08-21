package com.nyam.everyday.module.challenge.checker.event.listener;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import com.nyam.everyday.module.challenge.checker.ChallengeChecker;
import com.nyam.everyday.module.challenge.checker.event.event.ChallengeClearedEvent;
import com.nyam.everyday.module.challenge.checker.event.event.ProgressRecomputeEvent;
import com.nyam.everyday.module.challenge.checker.registry.ChallengeCheckerRegistry;
import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


/**
 * 어떤 챌린지의 조건을 만족해 챌린지 완성도를 다시 확인할 필요가 있을 때 발행되는 이벤트를 처리하는 리스너입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProgressRecomputeListener {

  private final ChallengeCheckService challengeCheckService;
  private final ChallengeCheckerRegistry challengeCheckerRegistry;
  private final ApplicationEventPublisher publisher;

  @Transactional(propagation = REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void progressRecompute(ProgressRecomputeEvent progressRecomputeEvent) {
    log.info("ProgressRecompute 동작");
    Member member = progressRecomputeEvent.getMember();
    Challenge challenge = progressRecomputeEvent.getChallenge();

    // 특정 챌린지, 유저 기반으로 해당 유저가 며칠이나 해당 챌린지를 달성했는지 다시 계산하고, 그 값을 업데이트한다.
    Integer progressCount = challengeCheckService.reComputeProgressCount(member, challenge);

    // 챌린지 체커를 가져온다
    ChallengeChecker checker = challengeCheckerRegistry.getChecker(challenge);

    // 체커가 조건을 달성했는지 확인함
    if (checker.isSatisfied(progressCount)) {
      // 챌린지 달성 이벤트 발행
      publisher.publishEvent(new ChallengeClearedEvent(member, challenge));
    }
  }

}
