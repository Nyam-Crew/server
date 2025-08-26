package com.nyam.everyday.module.challenge.checker;

import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.repository.ChallengeRepository;
import com.nyam.everyday.module.challenge.repository.MemberChallengeDayRepository;
import com.nyam.everyday.module.challenge.service.MemberChallengeStatusService;
import com.nyam.everyday.module.member.entity.Member;
import java.time.LocalDate;
import org.springframework.context.ApplicationEventPublisher;

// 이벤트 챌린지는 기본적으로 N일 이상 @@ 하기가 조건이므로, DateBasedChecker 상속
public abstract class AbstractEventChallengeChecker extends AbstractDateBasedChecker implements
    EventChallengeChecker {

  private final ChallengeRepository challengeRepository;
  private final ChallengeCheckService challengeCheckService;
  private final MemberChallengeStatusService memberChallengeStatusService;

  protected AbstractEventChallengeChecker(ChallengeRepository challengeRepository,
      ChallengeCheckService challengeCheckService,
      MemberChallengeDayRepository memberChallengeDayRepository,
      ApplicationEventPublisher publisher,
      MemberChallengeStatusService memberChallengeStatusService
  ) {
    super(challengeRepository, challengeCheckService, memberChallengeDayRepository, publisher);
    this.challengeRepository = challengeRepository;
    this.challengeCheckService = challengeCheckService;
    this.memberChallengeStatusService = memberChallengeStatusService;
  }

  public boolean dateCheck(Challenge challenge, LocalDate targetDate) {
    // 이벤트 챌린지는 챌린지 수행 가능 기간에 해당하는지 체크 필요
    LocalDate now = LocalDate.now();
    LocalDate startDate = LocalDate.from(challenge.getStartDate());
    LocalDate endDate = LocalDate.from(challenge.getEndDate());
    return !(now.isBefore(startDate)) && !(now.isAfter(endDate));
  }

  @Override
  public void check(Member member, LocalDate targetDate) {
    // 공통 : 코드 → 챌린지 로딩
    Challenge challenge = challengeRepository.getByChallengeCode(getChallengeCode());

    // 공통 : 검사 필요 여부 게이트
    if (!challengeCheckService.needToCheckChallenge(member, challenge)) {
      return;
    }

    // 이벤트 챌린지는 진행 가능 기간인지 체크 필요
    if (!dateCheck(challenge, targetDate)) {
      return;
    }

    // 사용자가 현재 해당 챌린지에 참여중인지도 확인 필요
    if (!memberChallengeStatusService.isAttending(member.getMemberId(), challenge.getId())) {
      return;
    }

    // 개별 로직 실행
    doCheckAndPublish(member, challenge, targetDate);
  }
}
