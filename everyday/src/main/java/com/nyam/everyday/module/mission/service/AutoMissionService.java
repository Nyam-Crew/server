package com.nyam.everyday.module.mission.service;

import com.nyam.everyday.module.mission.entity.*;
import com.nyam.everyday.module.mission.repository.DailyMissionRepository;
import com.nyam.everyday.module.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AutoMissionService {

    private final MissionRepository missionRepository;
    private final DailyMissionRepository dailyMissionRepository;
    private final SummaryQuery summaryQuery;
    private final StampService stampService; // 이미 사용하는 스탬프 업서트 서비스
    private final Clock clock;

    /** 특정 회원/날짜에 대해 AUTO 미션을 평가하고 완료 처리 */
    @Transactional
    public void evaluateForMember(Long memberId, LocalDate date) {
        List<Mission> autos = missionRepository.findByTypeAndIsActiveTrue(MissionType.AUTO);
        for (Mission m : autos) {
            boolean achieved = switch (m.getCategory()) {
                case WATER_1L      -> summaryQuery.getTotalWater(memberId, date) >= 1000.0;
                case MEAL_LOGGED   -> summaryQuery.existsMealLog(memberId, date);
                case WEIGHT_LOGGED -> summaryQuery.hasWeight(memberId, date);
                default -> false; // AUTO 는 위 3가지만
            };

            if (achieved) {
                var list = dailyMissionRepository.findByMemberIdAndMissionDate(memberId, date);
                list.stream()
                        .filter(dm -> dm.getMission().getMissionId().equals(m.getMissionId()))
                        .filter(dm -> !dm.isCompleted())
                        .forEach(dm -> {
                            dm.setCompleted(true);
                            dm.setCompletedBy(CompletedBy.AUTO);
                            dm.setCompletedDate(LocalDateTime.now(clock));
                        });
            }
        }

        // 스탬프 재집계
        int completedCount = dailyMissionRepository
                .countByMemberIdAndMissionDateAndIsCompletedTrue(memberId, date);
        stampService.upsertStamp(memberId, date, completedCount);
    }

}