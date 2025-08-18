package com.nyam.everyday.module.team.util;

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

    public static String meal(long memberId, long mealLogId) {
        return "MEAL:%d:%d".formatted(memberId, mealLogId);
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