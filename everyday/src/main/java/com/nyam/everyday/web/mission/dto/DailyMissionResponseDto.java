package com.nyam.everyday.web.mission.dto;

import com.nyam.everyday.module.mission.entity.CompletedBy;
import com.nyam.everyday.module.mission.entity.MissionCategory;
import com.nyam.everyday.module.mission.entity.MissionType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class DailyMissionResponseDto {
    Long dailyMissionId;
    Long missionId;
    MissionCategory category;
    String title;
    MissionType type;
    LocalDate missionDate;
    boolean completed;
    CompletedBy completedBy;
}