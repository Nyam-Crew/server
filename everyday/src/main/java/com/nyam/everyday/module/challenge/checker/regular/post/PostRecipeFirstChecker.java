package com.nyam.everyday.module.challenge.checker.regular.post;

import com.nyam.everyday.module.board.repository.BoardRepository;
import com.nyam.everyday.module.challenge.checker.ChallengeChecker;
import com.nyam.everyday.module.challenge.checker.event.event.ProgressRecomputeEvent;
import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCheckType;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.challenge.repository.ChallengeRepository;
import com.nyam.everyday.module.member.entity.Member;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostRecipeFirstChecker implements ChallengeChecker {

  private final ChallengeRepository challengeRepository;
  private final ChallengeCheckService challengeCheckService;
  private final ApplicationEventPublisher publisher;
  private final BoardRepository boardRepository;

  @Override
  public ChallengeCode getChallengeCode() {
    return ChallengeCode.POST_REPIPE_FIRST;
  }

  @Override
  public ChallengeTag getChallengeTag() {
    return ChallengeTag.POST;
  }

  @Override
  public ChallengeCheckType getChallengeCheckType() {
    return ChallengeCheckType.BY_COUNT;
  }

  @Override
  public void check(Member member, LocalDate targetDate) {
    Challenge challenge = challengeRepository.getByChallengeCode(this.getChallengeCode());

    // 횟수 기반 챌린지는 횟수 체크 필요한지만 확인하고 바로 이벤트 발행
    if (challengeCheckService.needToCheckChallenge(member, challenge)) return;

    publisher.publishEvent(new ProgressRecomputeEvent(member, challenge));
  }

  @Override
  public Long getProgress(Member member) {
    // 작성한 포스트 중, RECIPE갯수를 세서 반환한다.
    return boardRepository.getCountByMemberIdAndBoardType(member.getMemberId(), "recipe");
  }

  @Override
  public Boolean isSatisfied(Integer progressCount) {
    return progressCount > 0;
  }
}
