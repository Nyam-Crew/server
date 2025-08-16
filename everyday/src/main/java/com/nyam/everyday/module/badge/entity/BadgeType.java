package com.nyam.everyday.module.badge.entity;

import lombok.Getter;

/**
 * 뱃지 타입 Enum
 */
@Getter
public enum BadgeType {

    REGULAR_CHALLENGE(1),     // 상시 챌린지 (1점)
    EVENT_CHALLENGE(3);       // 이벤트 챌린지 (3점)

    private final int score;

    BadgeType(int score) {
        this.score = score;
    }
}
