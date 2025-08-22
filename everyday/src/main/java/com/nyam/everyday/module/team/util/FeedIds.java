package com.nyam.everyday.module.team.util;

import com.nyam.everyday.module.meal.type.MealType;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 *
 * FeedId 포맷 통합
 *
 * @author : 이지은
 * @fileName : FeedIds
 * @since : 25. 8. 18.
 *
 */
public class FeedIds {
    private FeedIds() {}

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 특정 사용자의 특정 날짜, 특정 식사 시간대 전체를 대표하는 피드 ID.
     * 예: "MEAL_PERIOD:101:20250821:BREAKFAST"
     */
    public static String mealPeriod(long memberId, LocalDate date, MealType mealType) {
        return "MEAL_PERIOD:%d:%s:%s".formatted(memberId, date.format(DATE_FORMATTER), mealType.name());
    }

    /**
     * ✅ 팀 공지사항을 위한 피드 ID.
     * 예: "NOTICE:501:1234" (501번 팀의 1234번 공지)
     */
    public static String notice(long teamId, long noticeId) {
        return "NOTICE:%d:%d".formatted(teamId, noticeId);
    }

    // java.util.Date를 받는 편의 메서드 (MealLogService에서 변환 코드를 줄일 수 있음)
    public static String mealPeriod(long memberId, Date  date, MealType mealType) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return mealPeriod(memberId, localDate, mealType);
    }
    public static String water(long memberId, long waterLogId) {
        return "WATER:%d:%d".formatted(memberId, waterLogId);
    }
    public static String weight(long memberId, long summaryId) {
        return "WEIGHT:%d:%d".formatted(memberId, summaryId);
    }
    public static String challenge(long memberId, long mcsId) {
        return "CHALLENGE:%d:%d".formatted(memberId, mcsId);
    }

}