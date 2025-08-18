package com.nyam.everyday.module.scorelog.entity;

/**
 * scoreLogs를 얻을 수 있는 조건 Enum
 */
public enum PointType {
    ATTENDANCE(1), //출석
    STAMP(1), //데일리 미션
    MEAL_INPUT(1),
    STATIC_BADGE_REWARD(1),
    SPECIAL_BADGE_REWARD(3),
    WATER_INTAKE(1);



    private final int defaultPoint;
    PointType(int p) { this.defaultPoint = p; }
    public int defaultPoint() { return defaultPoint; }
}