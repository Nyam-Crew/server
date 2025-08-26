package com.nyam.everyday.module.challenge.checker;

import com.nyam.everyday.module.challenge.checker.event.event.ProgressRecomputeEvent;
import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.repository.ChallengeRepository;
import com.nyam.everyday.module.member.entity.Member;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;

public abstract class AbstractCountBasedChecker extends AbstractChallengeChecker implements CountBasedChecker {

    private final ApplicationEventPublisher publisher;

    // 생성자에서 필요한 필드 주입
    protected AbstractCountBasedChecker(
            ChallengeRepository challengeRepository,
            ChallengeCheckService challengeCheckService,
            ApplicationEventPublisher publisher
    ) {
        super(challengeRepository, challengeCheckService);
        this.publisher = publisher;
    }
    
    
    // 횟수 기반 Checker는 별도의 체킹 필요 없이 바로 진행도 재계산 이벤트 발행
    @Override
    protected void doCheckAndPublish(Member member, Challenge challenge, LocalDate targetDate) {
        publisher.publishEvent(new ProgressRecomputeEvent(member, challenge));
    }
}
