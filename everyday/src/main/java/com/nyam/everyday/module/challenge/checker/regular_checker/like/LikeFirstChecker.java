package com.nyam.everyday.module.challenge.checker.regular_checker.like;

import com.nyam.everyday.module.boardLike.repository.BoardLikeRepository;
import com.nyam.everyday.module.challenge.checker.AbstractCountBasedChecker;
import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.challenge.repository.ChallengeRepository;
import com.nyam.everyday.module.member.entity.Member;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class LikeFirstChecker extends AbstractCountBasedChecker {

  private final BoardLikeRepository boardLikeRepository;

  protected LikeFirstChecker(
          ChallengeRepository challengeRepository,
          ChallengeCheckService challengeCheckService,
          ApplicationEventPublisher publisher,
          BoardLikeRepository boardLikeRepository
  ) {
    super(challengeRepository, challengeCheckService, publisher);
    this.boardLikeRepository = boardLikeRepository;
  }

  @Override
  public ChallengeCode getChallengeCode() {
    return ChallengeCode.LIKE_FIRST;
  }

  @Override
  public ChallengeTag getChallengeTag() {
    return ChallengeTag.LIKE;
  }

  @Override
  public long getProgress(Member member, Challenge challenge) {
    // 내가 누른 좋아요 갯수를 세서 반환한다
    return boardLikeRepository.countByMember_MemberId(member.getMemberId());
  }
}
