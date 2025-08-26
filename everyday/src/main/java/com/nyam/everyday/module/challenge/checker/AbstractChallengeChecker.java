package com.nyam.everyday.module.challenge.checker;

import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.repository.ChallengeRepository;
import com.nyam.everyday.module.member.entity.Member;

import java.time.LocalDate;

public abstract class AbstractChallengeChecker implements ChallengeChecker {

    private final ChallengeRepository challengeRepository;
    private final ChallengeCheckService challengeCheckService;

    protected AbstractChallengeChecker(
            ChallengeRepository challengeRepository,
            ChallengeCheckService challengeCheckService
    ) {
        this.challengeRepository = challengeRepository;
        this.challengeCheckService = challengeCheckService;
    }

    @Override
    public final void check(Member member, LocalDate targetDate) {
        // 공통: 코드 → 챌린지 로딩
        Challenge challenge = challengeRepository.getByChallengeCode(getChallengeCode());

        // 공통: 검사 필요 여부 게이트
        if (!challengeCheckService.needToCheckChallenge(member, challenge)) return;

        // 개별 로직 실행
        doCheckAndPublish(member, challenge, targetDate);
    }

    // 서브클래스가 구현할 핵심 로직
    protected abstract void doCheckAndPublish(Member member, Challenge challenge, LocalDate targetDate);
}
