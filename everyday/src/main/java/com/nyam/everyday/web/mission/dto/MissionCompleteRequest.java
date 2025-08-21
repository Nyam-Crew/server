package com.nyam.everyday.web.mission.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public final class MissionCompleteRequest {
    private boolean complete;
}