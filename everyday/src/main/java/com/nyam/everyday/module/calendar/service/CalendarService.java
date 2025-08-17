package com.nyam.everyday.module.calendar.service;

import com.nyam.everyday.module.mission.entity.DailyMissionStamp;
import com.nyam.everyday.module.mission.repository.DailyMissionStampRepository;
import com.nyam.everyday.module.summary.entity.MemberDailySummary;
import com.nyam.everyday.module.summary.repository.MemberDailySummaryRepository;
import com.nyam.everyday.web.calendar.dto.CalendarDayDto;
import com.nyam.everyday.web.calendar.dto.MonthlyCalendarResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final MemberDailySummaryRepository summaryRepository;
    private final DailyMissionStampRepository stampRepository;
    private final Clock clock; // Asia/Seoul

    @Transactional(readOnly = true)
    public MonthlyCalendarResponse getMonthly(Long memberId, Integer year, Integer month) {
        // 기본값: 오늘 기준
        LocalDate today = LocalDate.now(clock);
        int y = (year == null) ? today.getYear() : year;
        int m = (month == null) ? today.getMonthValue() : month;
        YearMonth ym = YearMonth.of(y, m);

        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();

        // 1) 요약/스탬프 한 번에 로딩
        List<MemberDailySummary> summaries =
                summaryRepository.findAllByMember_MemberIdAndSummaryDateBetweenOrderBySummaryDateAsc(
                        memberId, start, end);

        List<DailyMissionStamp> stamps =
                stampRepository.findByMemberIdAndMissionDateBetween(memberId, start, end);

        // 2) map으로 빠른 조회 준비
        Map<LocalDate, MemberDailySummary> summaryMap = summaries.stream()
                .collect(Collectors.toMap(MemberDailySummary::getSummaryDate, s -> s));

        Set<LocalDate> achievedDays = stamps.stream()
                .filter(DailyMissionStamp::isAchieved)
                .map(DailyMissionStamp::getMissionDate)
                .collect(Collectors.toSet());

        // 3) 월 전체 날짜 loop 채우기(없으면 기본값)
        List<CalendarDayDto> items = new ArrayList<>(ym.lengthOfMonth());
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            MemberDailySummary s = summaryMap.get(d);

            Integer kcal = (s != null && s.getTotalKcal() != null) ? s.getTotalKcal() : 0;
            BigDecimal weight = (s != null) ? s.getWeight() : null;
            Integer water = (s != null && s.getTotalWater() != null) ? s.getTotalWater().intValue() : null;

            items.add(CalendarDayDto.builder()
                    .date(d)
                    .kcal(kcal)
                    .weight(weight)
                    .water(water)
                    .achieved(achievedDays.contains(d))
                    .build());
        }

        return MonthlyCalendarResponse.builder()
                .year(y)
                .month(m)
                .days(ym.lengthOfMonth())
                .items(items)
                .build();
    }
}