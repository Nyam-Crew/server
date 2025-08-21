package com.nyam.everyday.module.team.view;

import com.nyam.everyday.module.meal.type.MealType;
import com.nyam.everyday.web.team.dto.TeamActivityFeedItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * 피드 메세지 포맷 설정을 위한 클래스
 *
 * @author : 이지은
 * @fileName : TeamFeedMessageFormatter
 * @since : 25. 8. 18.
 *
 */
public class TeamFeedMessageFormatter {

    private TeamFeedMessageFormatter() {}

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm").withLocale(Locale.KOREAN);

    /** TeamActivityFeedItem → 카톡 스타일 한 줄 메시지 */
    public static String formatLine(TeamActivityFeedItem item) {
        final String nick = nullToEmpty(item.getNickname());
        final String time = timeText(item.getFeedCreatedDate());

        String base = switch (item.getActivityType()) {
            case MEAL      -> formatMeal(nick, item.getMealPeriod(), item.getKcal(), time);
            case WATER     -> formatWater(nick, item.getAmountMl(), time);
            case WEIGHT    -> formatWeight(nick, item.getDeltaKg(), time);
            case CHALLENGE -> formatChallenge(nick, item.getChallengeName(), time);
            case NOTICE    -> formatNotice(time); // ✅ 공지사항 케이스 추가
        };

        // 필요 시 " · 수정됨" 꼬리표
        if (item.isModified()) {
            base = base + " · 수정됨";
        }
        return base;
    }

    /** 식단: "닉네임 아침기록 250kcal 먹었어요 등록된 시간" */
    private static String formatMeal(String nickname, MealType period, BigDecimal kcal, String time) {
        String label = (period != null ? period.getLabel() : "식단");
        String kcalTxt = (kcal != null ? kcal + "kcal" : "");
        return "%s %s기록 %s 먹었어요 %s".formatted(nickname, label, kcalTxt, time).trim();
    }

    /** 물: "닉네임 물섭취기록 0.5L 마셨어요 등록된 시간" */
    private static String formatWater(String nickname, Integer amountMl, String time) {
        String literTxt = (amountMl != null ? toLiterText(amountMl) : "");
        return "%s 물섭취기록 %s 마셨어요 %s".formatted(nickname, literTxt, time).trim();
    }

    /** 체중: "닉네임 체중기록 0.5kg 감량했어요|증가했어요 등록 시간" */
    private static String formatWeight(String nickname, Double deltaKg, String time) {
        if (deltaKg == null || Math.abs(deltaKg) < 1e-6) {
            return "%s 체중기록 변화 없음 %s".formatted(nickname, time);
        }
        double abs = Math.abs(deltaKg);
        String dir = (deltaKg < 0) ? "감량했어요" : "증가했어요";
        return "%s 체중기록 %.1fkg %s %s".formatted(nickname, abs, dir, time);
    }

    /** 챌린지: "닉네임 챌린지달성 xx 챌린지를 완료했어요 등록시간" */
    private static String formatChallenge(String nickname, String challengeName, String time) {
        String name = nullToEmpty(challengeName);
        return "%s 챌린지달성 %s 챌린지를 완료했어요 %s".formatted(nickname, name, time).trim();
    }

    private static String timeText(LocalDateTime dt) {
        return (dt == null) ? "등록시간 미상" : TIME_FMT.format(dt);
    }

    /** 500(ml) → "0.5L", 1000 → "1L" */
    private static String toLiterText(int ml) {
        if (ml % 1000 == 0) return (ml / 1000) + "L";
        return String.format(Locale.KOREAN, "%.1fL", ml / 1000.0);
    }

    /**
     * ✅ 공지사항: "새로운 공지가 등록되었습니다. 등록된 시간"
     */
    private static String formatNotice(String time) {
        return "새로운 공지가 등록되었습니다. %s".formatted(time).trim();
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}