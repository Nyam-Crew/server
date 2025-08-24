package com.nyam.everyday.module.team.enums;

/**
 *
 * 알림 전송 상태 Enum
 *
 * @author : 이지은
 * @fileName : DeleveryStatus
 * @since : 25. 8. 20.
 *
 */
public enum DeliveryStatus {
    PENDING,  // 처리 대기
    IMMEDIATE, // 즉시 발송 완료
    BATCHED,    // 요약 알림으로 변경 완료
    PROCESSED; // 요약 처리가 완료되어 발송까지 완료
}
