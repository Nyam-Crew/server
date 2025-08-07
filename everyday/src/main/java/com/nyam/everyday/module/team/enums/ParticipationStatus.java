package com.nyam.everyday.module.team.enums;

/**
 *
 * 그룹 참가 신청
 *
 * @author : 이지은
 * @fileName : ParticipationStatus
 * @since : 25. 8. 7.
 *
 */
public enum ParticipationStatus {
    NOT_JOINED,    // 가입 요청 안함
    PENDING,       // 가입 요청 중
    APPROVED,      // 가입 완료
    REJECTED,      // 거절됨
    BANNED;
}



