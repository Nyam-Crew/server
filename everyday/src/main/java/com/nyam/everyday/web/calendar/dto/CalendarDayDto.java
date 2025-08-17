package com.nyam.everyday.web.calendar.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CalendarDayDto {
    private LocalDate date;
    private Integer kcal;            // 없으면 0
    private BigDecimal weight;       // 없으면 null
    private Integer water;           // ml, 없으면 null
    private boolean achieved;        // 스탬프 달성 여부
}