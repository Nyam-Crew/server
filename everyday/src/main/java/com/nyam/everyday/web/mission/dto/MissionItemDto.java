package com.nyam.everyday.web.mission.dto;

import com.nyam.everyday.module.mission.entity.MissionCategory;
import com.nyam.everyday.module.mission.entity.MissionType;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class MissionItemDto {
    private Long dailyMissionId;
    private Long missionId;
    private MissionCategory category;
    private String title;
    private MissionType type;
    private boolean completed;
    private String completedBy; // AUTO/MANUAL/NONE 문자열
    private LocalDateTime completedDate;
}