package com.nyam.everyday.web.mission.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public final class MissionCompleteRequestDto {
    private boolean complete;
}