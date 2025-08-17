package com.nyam.everyday.module.mission.service;

import com.nyam.everyday.module.mission.entity.DailyMissionStamp;
import com.nyam.everyday.module.mission.repository.DailyMissionRepository;
import com.nyam.everyday.module.mission.repository.DailyMissionStampRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StampService {
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
            // issuedDate는 최초 발급 시각 유지. (원하면 업데이트로 바꿔도 됨)
        }
    }
}