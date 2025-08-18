package com.nyam.everyday.module.mission.service;

import com.nyam.everyday.module.mission.entity.CompletedBy;
import com.nyam.everyday.module.mission.entity.DailyMission;
import com.nyam.everyday.module.mission.repository.DailyMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MissionProgressService {

    private final DailyMissionRepository dailyMissionRepository;
    private final StampService stampService;
    private final Clock clock;

    /**
     * MANUAL 완료/해제 토글 후, 해당 날짜 스탬프 업서트(2개 기준)
     */
    @Transactional
    public void toggleManualCompletion(Long dailyMissionId, Long memberId, boolean complete) {
        DailyMission dm = dailyMissionRepository.findByDailyMissionIdAndMemberId(dailyMissionId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("미션이 없거나 본인 소유가 아닙니다."));

        if (complete) {
            if (!dm.isCompleted()) {
                dm.setCompleted(true);
                dm.setCompletedBy(CompletedBy.MANUAL);
                dm.setCompletedDate(LocalDateTime.now(clock));
            }
        } else {
            if (dm.isCompleted()) {
                dm.setCompleted(false);
                dm.setCompletedBy(CompletedBy.NONE);
                dm.setCompletedDate(null);
            }
        }
        // ✅ 스탬프 재집계 & 업서트
        LocalDate date = dm.getMissionDate();
        int completedCount = dailyMissionRepository
                .countByMemberIdAndMissionDateAndIsCompletedTrue(memberId, date);
        stampService.upsertStamp(memberId, date, completedCount); // ✅ 올바른 메서드 사용
    }
}