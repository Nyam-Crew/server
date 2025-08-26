package com.nyam.everyday.module.mission.service;

import com.nyam.everyday.module.challenge.checker.event.event.ChallengeCheckEvent;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.mission.entity.DailyMissionStamp;
import com.nyam.everyday.module.mission.repository.DailyMissionStampRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StampService {

    private final ApplicationEventPublisher publisher;
    private final DailyMissionStampRepository stampRepository;
    private final Clock clock;

    @Transactional
    public void upsertStamp(Long memberId, LocalDate date, int completedCount) {
        var opt = stampRepository.findByMemberIdAndMissionDate(memberId, date);
        boolean achieved = completedCount >= 2;

        if (opt.isEmpty()) {
            DailyMissionStamp s = DailyMissionStamp.builder()
                    .memberId(memberId)
                    .missionDate(date)
                    .completedCount(completedCount)
                    .achieved(achieved)
                    .issuedDate(LocalDateTime.now(clock))
                    .build();
            stampRepository.save(s);
        } else {
            DailyMissionStamp s = opt.get();
            s.setCompletedCount(completedCount);
            s.setAchieved(achieved);
        }

        publisher.publishEvent(new ChallengeCheckEvent(memberId, ChallengeTag.STAMP, date));
    }
}