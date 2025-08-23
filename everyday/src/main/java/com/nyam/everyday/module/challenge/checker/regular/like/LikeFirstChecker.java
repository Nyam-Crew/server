package com.nyam.everyday.module.challenge.checker.regular.like;

import com.nyam.everyday.module.boardLike.repository.BoardLikeRepository;
import com.nyam.everyday.module.challenge.checker.ChallengeChecker;
import com.nyam.everyday.module.challenge.checker.event.event.ProgressRecomputeEvent;
import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCheckType;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.member.entity.Member;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeFirstChecker implements ChallengeChecker {

  private final ChallengeCheckService challengeCheckService;
  private final BoardLikeRepository boardLikeRepository;
  private final ApplicationEventPublisher publisher;


  @Override
  public ChallengeCode getChallengeCode() {
    return ChallengeCode.LIKE_FIRST;
  }

  @Override
  public ChallengeTag getChallengeTag() {
    return ChallengeTag.LIKE;
  }

  @Override
  public ChallengeCheckType getChallengeCheckType() {
    return ChallengeCheckType.BY_COUNT;
  }

  @Override
  public void check(Member member, LocalDate targetDate) {

    // 챌린지 정보 불러오기
    Challenge challenge = challengeCheckService.getChallengeByChallengeCode(this.getChallengeCode());

    // 챌린지 체크해야 하나?
    if (!challengeCheckService.needToCheckChallenge(member, challenge)) return;

    // 체크해야 하면, 바로 ProgressCount 이벤트 발행(By Count 이벤트)
    publisher.publishEvent(new ProgressRecomputeEvent(member, challenge));
  }

  @Override
  public Long getProgress(Member member) {
    // 내가 처음으로 좋아요를 눌러야 하는 것이므로, 내가 누른 좋아요 갯수를 세서 반환한다
    return boardLikeRepository.countByMember_MemberId(member.getMemberId());
  }

  @Override
  public Boolean isSatisfied(Integer progressCount) {
    // 좋아요를 한번만 누르면 클리어다! 1 이상이면 true 반환
    return progressCount >= 1;
  }
}
