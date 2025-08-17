package com.nyam.everyday.web.calendar.dto;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MonthlyCalendarResponse {
    private int year;
    private int month;
    private int days;                 // YearMonth.lengthOfMonth()
    private List<CalendarDayDto> items;
}