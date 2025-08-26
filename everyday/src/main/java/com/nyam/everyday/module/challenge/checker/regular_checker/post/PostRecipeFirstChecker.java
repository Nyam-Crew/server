package com.nyam.everyday.module.challenge.checker.regular_checker.post;

import com.nyam.everyday.module.board.repository.BoardRepository;
import com.nyam.everyday.module.challenge.checker.AbstractCountBasedChecker;
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
public class PostRecipeFirstChecker extends AbstractCountBasedChecker {

  private final BoardRepository boardRepository;

  protected PostRecipeFirstChecker(
          ChallengeRepository challengeRepository,
          ChallengeCheckService challengeCheckService,
          ApplicationEventPublisher publisher,
          BoardRepository boardRepository
  ) {
    super(challengeRepository, challengeCheckService, publisher);
    this.boardRepository = boardRepository;
  }

  @Override
  public ChallengeCode getChallengeCode() {
    return ChallengeCode.POST_REPIPE_FIRST;
  }

  @Override
  public ChallengeTag getChallengeTag() {
    return ChallengeTag.POST;
  }

  @Override
  public long getProgress(Member member, Challenge challenge) {
    // 작성한 포스트 중, RECIPE갯수를 세서 반환한다.
    return boardRepository.getCountByMemberIdAndBoardType(member.getMemberId(), "recipe");
  }
}
