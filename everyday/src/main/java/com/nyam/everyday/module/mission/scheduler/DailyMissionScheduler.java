package com.nyam.everyday.module.mission.scheduler;

import com.nyam.everyday.module.mission.service.MissionAssignmentService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyMissionScheduler {

    private final MissionAssignmentService assignmentService;

    // 00:00 KST: 정리 + D+1 보정
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "rolloverAndAssignNextDay",
            lockAtLeastFor = "PT30S", lockAtMostFor = "PT10M")
    public void rolloverAndAssign() {
        assignmentService.rolloverAtMidnight();
    }
}