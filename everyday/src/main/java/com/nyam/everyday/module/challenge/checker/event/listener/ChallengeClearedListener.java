package com.nyam.everyday.module.challenge.checker.event.listener;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import com.nyam.everyday.module.badge.service.BadgeService;
import com.nyam.everyday.module.challenge.checker.event.event.ChallengeClearedEvent;
import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.MemberChallengeStatus;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.notification.entity.NotificationType;
import com.nyam.everyday.module.notification.service.NotificationService;
import com.nyam.everyday.web.badge.dto.AssignBadgeRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeClearedListener {

  private final ChallengeCheckService challengeCheckService;
  private final BadgeService badgeService;
  private final NotificationService notificationService;


  @Transactional(propagation = REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onChallengeClearedEvent(ChallengeClearedEvent event) {
    log.info("챌린지 클리어 리스너 동작");
    Member member = event.getMember();
    Challenge challenge = event.getChallenge();

    // 상태 가져오기
    MemberChallengeStatus mcs = challengeCheckService.getOrCreateMCS(member, challenge);

    /// 챌린지 클리어 처리
    // 챌린지 완성 상태로 만들기
    mcs.setAsCleared();

    // 뱃지 및 점수 추가
    AssignBadgeRequestDto badgeDto = new AssignBadgeRequestDto();
    badgeDto.setBadgeId(challenge.getId());
    badgeService.assignBadgeToMember(member.getMemberId(), badgeDto);

    // 사용자에게 알림 전송
    notificationService.addPrivateNotification(challenge.getTitle() + " 챌린지를 달성해 뱃지를 획득헀습니다.", member.getMemberId(), NotificationType.CHALLENGE_CLEAR);
  }

}
