package com.nyam.everyday.module.challenge.checker.event.listener;

import com.nyam.everyday.module.challenge.checker.event.event.MCDDeleteEvent;
import com.nyam.everyday.module.challenge.checker.event.event.ProgressRecomputeEvent;
import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.member.entity.Member;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MCDDeleteListener {

  private final ChallengeCheckService challengeCheckService;
  private final ApplicationEventPublisher publisher;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @EventListener
  public void onMCDDeleteEvent(MCDDeleteEvent event) {
    Member member = event.getMember();
    Challenge challenge = event.getChallenge();
    LocalDate targetDate = event.getTargetDate();

    // 해당 일자에 해당하는 mcd를 삭제한다.
    challengeCheckService.deleteMemberChallengeDay(member, challenge, targetDate);

    // 진행도를 다시 체크하도록 이벤트를 발행한다
    publisher.publishEvent(new ProgressRecomputeEvent(member, challenge));
  }
}
