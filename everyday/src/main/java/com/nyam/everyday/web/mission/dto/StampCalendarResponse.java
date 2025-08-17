package com.nyam.everyday.web.mission.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.Map;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class StampCalendarResponse {
    private Long memberId;
    private String month; // yyyy-MM
    /** key = date(yyyy-MM-dd), value = achieved */
    private Map<LocalDate, Boolean> stamps;
}