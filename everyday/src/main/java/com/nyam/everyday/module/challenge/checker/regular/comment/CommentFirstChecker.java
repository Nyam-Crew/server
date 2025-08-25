package com.nyam.everyday.module.challenge.checker.regular.comment;

import com.nyam.everyday.module.boardComment.entity.BoardComment;
import com.nyam.everyday.module.boardComment.repository.BoardCommentRepository;
import com.nyam.everyday.module.challenge.checker.ChallengeChecker;
import com.nyam.everyday.module.challenge.checker.event.event.ProgressRecomputeEvent;
import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCheckType;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.challenge.repository.ChallengeRepository;
import com.nyam.everyday.module.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class CommentFirstChecker implements ChallengeChecker {

    private final ChallengeRepository challengeRepository;
    private final ChallengeCheckService challengeCheckService;
    private final BoardCommentRepository boardCommentRepository;

    private final ApplicationEventPublisher publisher;

    @Override
    public ChallengeCode getChallengeCode() {
        return ChallengeCode.COMMENT_FIRST;
    }

    @Override
    public ChallengeTag getChallengeTag() {
        return ChallengeTag.COMMENT;
    }

    @Override
    public ChallengeCheckType getChallengeCheckType() {
        return ChallengeCheckType.BY_COUNT;
    }

    @Override
    public void check(Member member, LocalDate targetDate) {
        Challenge challenge = challengeRepository.getByChallengeCode(this.getChallengeCode());

        if (!challengeCheckService.needToCheckChallenge(member, challenge)) return;
        
        publisher.publishEvent(new ProgressRecomputeEvent(member, challenge));
    }

    @Override
    public Long getProgress(Member member) {
        // 내가 작성한 댓글 수 세서 반환하기
        return boardCommentRepository.getCountByMemberId(member.getMemberId());
    }

    @Override
    public Boolean isSatisfied(Integer progressCount) {
        // 1회 이상 댓글 작성했으면 완료
        return progressCount > 0;
    }
}
