package com.nyam.everyday.module.challenge.checker.event.listener;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import com.nyam.everyday.module.challenge.checker.ChallengeChecker;
import com.nyam.everyday.module.challenge.checker.event.event.ChallengeClearedEvent;
import com.nyam.everyday.module.challenge.checker.event.event.ProgressRecomputeEvent;
import com.nyam.everyday.module.challenge.checker.registry.ChallengeCheckerRegistry;
import com.nyam.everyday.module.challenge.checker.service.ProgressComputeService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCheckType;
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

  private final ProgressComputeService progressComputeService;
  private final ChallengeCheckerRegistry challengeCheckerRegistry;
  private final ApplicationEventPublisher publisher;

  @Transactional(propagation = REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void progressRecompute(ProgressRecomputeEvent progressRecomputeEvent) {
    log.info("ProgressRecompute 동작");
    Member member = progressRecomputeEvent.getMember();
    Challenge challenge = progressRecomputeEvent.getChallenge();

    // 챌린지 체커를 가져온다
    ChallengeChecker checker = challengeCheckerRegistry.getChecker(challenge);

    // 챌린지에는 두 종류가 있음. 먼저 일수 기반 챌린지의 경우, 몇일이나 달성했는지 세면 된다
    if (checker.getChallengeCheckType() == ChallengeCheckType.BY_DAY) {
      // 특정 챌린지, 유저 기반으로 해당 유저가 며칠이나 해당 챌린지를 달성했는지 다시 계산하고, 그 값을 업데이트한다.
      Integer progressCount = progressComputeService.computeProgressCountByDay(member, challenge);

      // 체커가 조건을 달성했는지 확인함
      if (checker.isSatisfied(progressCount)) {
        // 챌린지 달성 이벤트 발행
        publisher.publishEvent(new ChallengeClearedEvent(member, challenge));
      }
    }

    // 횟수 기반 챌린지의 경우, 체커 내부에 Progress 체킹 로직이 있다. 이를 기반으로 체크한다
    else {
      // 유저가 특정 행동을 얼마나 했는지를 내부에서 계산한다. (글 작성, 좋아요 누르기 등)
      Integer progressCount = progressComputeService.computeProgressCountByCount(member, challenge);

      // 체커 기반으로 조건을 달성했는지 확인한다
      if (checker.isSatisfied(progressCount)) {
        // 챌린지 달성 이벤트 발행
        publisher.publishEvent(new ChallengeClearedEvent(member, challenge));
      }
    }
  }
}
