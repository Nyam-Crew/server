package com.nyam.everyday.module.challenge.checker;

import com.nyam.everyday.module.challenge.checker.event.event.MCDCreateEvent;
import com.nyam.everyday.module.challenge.checker.event.event.MCDDeleteEvent;
import com.nyam.everyday.module.challenge.checker.service.ChallengeCheckService;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.repository.ChallengeRepository;
import com.nyam.everyday.module.challenge.repository.MemberChallengeDayRepository;
import com.nyam.everyday.module.member.entity.Member;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;

public abstract class AbstractDateBasedChecker extends AbstractChallengeChecker implements DateBasedChecker {

    private final ApplicationEventPublisher publisher;
    private final MemberChallengeDayRepository memberChallengeDayRepository;

    protected AbstractDateBasedChecker(
            int threshold,
            ChallengeRepository challengeRepository,
            ChallengeCheckService challengeCheckService,
            MemberChallengeDayRepository memberChallengeDayRepository,
            ApplicationEventPublisher publisher
    ) {
        super(threshold, challengeRepository, challengeCheckService);
        this.memberChallengeDayRepository = memberChallengeDayRepository;
        this.publisher = publisher;
    }

    // 날짜 기반 챌린지는 모두 같은 방법으로 진행도 체크할 것 (날짜 세기)
    @Override
    public long getProgress(Member member, Challenge challenge) {
        Long memberId = member.getMemberId();
        Long challengeId = challenge.getId();

        // 멤버가 특정 챌린지를 깬 날짜가 총 몇일인지 확인하고, 반환한다.
        return memberChallengeDayRepository.countMemberChallengeDay(memberId,
                challengeId);
    }

    // MCDCreateEvent 발행
    protected void publishMCDCreateEvent(Member member, Challenge challenge, LocalDate targetDate) {
        publisher.publishEvent(new MCDCreateEvent(member, challenge, targetDate));
    }

    // MCDDeleteEvent 발행
    protected void publishMCDDeleteEvent(Member member, Challenge challenge, LocalDate targetDate) {
        publisher.publishEvent(new MCDDeleteEvent(member, challenge, targetDate));
    }
}
