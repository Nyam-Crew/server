package com.nyam.everyday.module.challenge.checker.event.listener;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import com.nyam.everyday.module.badge.service.BadgeService;
import com.nyam.everyday.module.challenge.checker.event.event.ChallengeClearedEvent;
import com.nyam.everyday.module.challenge.checker.service.MemberChallengeStatusService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.MemberChallengeStatus;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.notification.entity.NotificationType;
import com.nyam.everyday.module.notification.service.NotificationService;
import com.nyam.everyday.module.team.enums.ActivityType;
import com.nyam.everyday.module.team.service.TeamActivityFeedRedisService;
import com.nyam.everyday.module.team.service.TeamMemberService;
import com.nyam.everyday.module.team.util.FeedIds;
import com.nyam.everyday.web.badge.dto.AssignBadgeRequestDto;
import com.nyam.everyday.web.team.dto.TeamActivityFeedItem;
import java.time.Duration;
import java.util.Set;
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

  private final MemberChallengeStatusService memberChallengeStatusService;
  private final BadgeService badgeService;
  private final NotificationService notificationService;
  private final TeamActivityFeedRedisService feedService;
  private final TeamMemberService teamMemberService;

  // --- ▼ [수정] 피드 TTL을 30일에서 24시간으로 변경 ---
  private static final Duration FEED_TTL = Duration.ofHours(24);


  @Transactional(propagation = REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onChallengeClearedEvent(ChallengeClearedEvent event) {
    log.info("챌린지 클리어 리스너 동작");
    Member member = event.getMember();
    Challenge challenge = event.getChallenge();

    // 상태 가져오기
    MemberChallengeStatus mcs = memberChallengeStatusService.getOrCreateMCS(member, challenge);

    /// 챌린지 클리어 처리
    // 챌린지 완성 상태로 만들기
    mcs.setAsCleared();

    // 뱃지 및 점수 추가
    AssignBadgeRequestDto badgeDto = new AssignBadgeRequestDto();
    badgeDto.setBadgeId(challenge.getId());
    badgeService.assignBadgeToMember(member.getMemberId(), badgeDto);

    // 사용자에게 알림 전송
    notificationService.addPrivateNotification(challenge.getTitle() + " 챌린지를 달성해 뱃지를 획득헀습니다.",
        member.getMemberId(), NotificationType.CHALLENGE_CLEAR);

    // 1. 멤버가 '활동 중'인 모든 팀의 ID를 TeamMemberService를 통해 조회
    Set<Long> teamIds = teamMemberService.findTeamIdsByMember(member.getMemberId());

    // 2. 멤버가 활동 중인 팀이 없으면 피드를 생성하지 않음
    if (teamIds.isEmpty()) {
      log.warn("멤버 '{}'가 활동 중인 팀이 없어 챌린지 클리어 피드를 생성하지 않습니다.", member.getNickname());
      return;
    }

    // 피드 생성 로직
    final String feedId = FeedIds.challenge(member.getMemberId(), mcs.getId());
    final long createdAtMs = System.currentTimeMillis();
    TeamActivityFeedItem feedItem = TeamActivityFeedItem.builder()
        .memberId(member.getMemberId())
        .nickname(member.getNickname())
        .profileImageUrl(member.getMemberImg()) // getMemberImg() -> getProfileImageUrl() 오타 수정
        .activityType(ActivityType.CHALLENGE)
        .challengeName(challenge.getTitle())
        .build();
    feedService.addFeedItemToTeams(
        teamIds,
        feedId,
        createdAtMs,
        feedItem,
        FEED_TTL
    );
    log.info("'{}' 챌린지 클리어 피드 생성 완료 (대상 팀: {})", challenge.getTitle(), teamIds);
  }

}
