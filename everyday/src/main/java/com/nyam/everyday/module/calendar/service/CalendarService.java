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
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final MemberDailySummaryRepository summaryRepository;
    private final DailyMissionStampRepository stampRepository;
    private final Clock clock; // Asia/Seoul 주입

    @Transactional(readOnly = true)
    public MonthlyCalendarResponse getMonthly(Long memberId, Integer year, Integer month) {
        // 0) 기준 연/월 결정
        LocalDate today = LocalDate.now(clock);
        int y = (year == null) ? today.getYear() : year;
        int m = (month == null) ? today.getMonthValue() : month;
        YearMonth ym = YearMonth.of(y, m);

        // 1) 월의 시작/끝(LocalDate)
        LocalDate startLd = ym.atDay(1);
        LocalDate endLd   = ym.atEndOfMonth();

        // 2) Date 경계 계산: [fromInclusive, toExclusive)
        ZoneId zone = clock.getZone();
        Date fromInclusive = Date.from(startLd.atStartOfDay(zone).toInstant());
        Date toExclusive   = Date.from(ym.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant());

        // 3) 요약 로드 (Date 경계 사용)
        List<MemberDailySummary> summaries =
                summaryRepository
                        .findAllByMember_MemberIdAndSummaryDateGreaterThanEqualAndSummaryDateLessThanOrderBySummaryDateAsc(
                                memberId, fromInclusive, toExclusive
                        );

        // 4) 스탬프 로드 (스탬프는 LocalDate 컬럼이면 그대로 사용)
        List<DailyMissionStamp> stamps =
                stampRepository.findByMemberIdAndMissionDateBetween(memberId, startLd, endLd);

        // 5) 요약 맵: 키를 LocalDate로 통일
        Map<LocalDate, MemberDailySummary> summaryMap = new HashMap<>();
        for (MemberDailySummary s : summaries) {
            LocalDate key = s.getSummaryDate().toInstant().atZone(zone).toLocalDate();
            summaryMap.putIfAbsent(key, s); // 중복 시 첫 번째 유지
        }

        Set<LocalDate> achievedDays = stamps.stream()
                .filter(DailyMissionStamp::isAchieved)
                .map(DailyMissionStamp::getMissionDate)
                .collect(Collectors.toSet());

        // 6) 달력 채우기
        List<CalendarDayDto> items = new ArrayList<>(ym.lengthOfMonth());
        for (LocalDate d = startLd; !d.isAfter(endLd); d = d.plusDays(1)) {
            MemberDailySummary s = summaryMap.get(d);

            BigDecimal kcal   = (s != null) ? s.getTotalKcal() : null;
            BigDecimal weight = (s != null) ? s.getWeight()    : null;
            Integer water     = (s != null && s.getTotalWater() != null)
                    ? s.getTotalWater().intValue()
                    : null;

            items.add(CalendarDayDto.builder()
                    .date(d)
                    .kcal(kcal)
                    .weight(weight)
                    .water(water)
                    .achieved(achievedDays.contains(d))
                    .build());
        }

        // 7) 응답
        return MonthlyCalendarResponse.builder()
                .year(y)
                .month(m)
                .days(ym.lengthOfMonth())
                .items(items)
                .build();
    }
}