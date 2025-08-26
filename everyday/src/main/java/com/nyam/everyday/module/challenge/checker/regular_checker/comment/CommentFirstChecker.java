package com.nyam.everyday.module.challenge.checker.regular_checker.comment;

import com.nyam.everyday.module.boardComment.repository.BoardCommentRepository;
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
public class CommentFirstChecker extends AbstractCountBasedChecker {

    private final BoardCommentRepository boardCommentRepository;

    protected CommentFirstChecker(
            ChallengeRepository challengeRepository,
            ChallengeCheckService challengeCheckService,
            ApplicationEventPublisher publisher,
            BoardCommentRepository boardCommentRepository
    ) {
        super(challengeRepository, challengeCheckService, publisher);
        this.boardCommentRepository = boardCommentRepository;
    }

    @Override
    public ChallengeCode getChallengeCode() {
        return ChallengeCode.COMMENT_FIRST;
    }

    @Override
    public ChallengeTag getChallengeTag() {
        return ChallengeTag.COMMENT;
    }

    @Override
    public long getProgress(Member member, Challenge challenge) {
        // 내가 작성한 댓글 수 세서 반환하기
        return boardCommentRepository.getCountByMemberId(member.getMemberId());
    }
}
