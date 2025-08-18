package com.nyam.everyday.web.mission.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MissionCompleteRequest {
    private boolean complete;
}