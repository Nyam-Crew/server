package com.nyam.everyday.module.scorelog.entity;

/**
 *
 * 각 행동별 스코어
 *
 * @author : 이지은
 * @fileName : ScoreType
 * @since : 25. 8. 19.
 *
 */
public enum ScoreType {
    ATTENDANCE(1),       // 출석
    STAMP(1),            // 도장
    BADGE_REWARD(1),     // 뱃지(상시) - 팀원이 구현해둔 로직에서 필요시 명시 포인트로 덮어쓰기 가능
    CHALLENGE_EVENT(3),  // 이벤트/상시 챌린지
    MEAL_LOG(1),         // 식단 입력
    WATER_INTAKE(1);     // 물 입력

    private final int defaultPoint;
    ScoreType(int score) { this.defaultPoint = score; }
    public int defaultPoint() { return defaultPoint; }
}
