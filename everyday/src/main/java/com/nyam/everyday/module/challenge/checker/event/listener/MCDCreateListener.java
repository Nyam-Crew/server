package com.nyam.everyday.module.challenge.checker.event.listener;

import com.nyam.everyday.module.challenge.checker.event.event.MCDCreateEvent;
import com.nyam.everyday.module.challenge.checker.event.event.ProgressRecomputeEvent;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.service.MemberChallengeDayService;
import com.nyam.everyday.module.member.entity.Member;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MCDCreateListener {

  private final MemberChallengeDayService memberChallengeDayService;
  private final ApplicationEventPublisher publisher;

  @EventListener
  @Async("challengeExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void onMCDCreateEvent(MCDCreateEvent event) {
    log.info("MCD 리스너 동작");
    Member member = event.getMember();
    Challenge challenge = event.getChallenge();
    LocalDate targetDate = event.getTargetDate();

    // 해당 일자에 해당하는 MemberChallengeDay를 만든다.
    memberChallengeDayService.addMemberChallengeDay(member, challenge, targetDate);

    // 진행도를 다시 체크하도록 이벤트를 발행한다
    publisher.publishEvent(new ProgressRecomputeEvent(member, challenge));
  }

}
