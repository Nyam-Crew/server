package com.nyam.everyday.common.alert.service;

import com.nyam.everyday.common.alert.dto.AlertDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class AlertService {

  private final SimpMessagingTemplate simpMessagingTemplate;

  /**
   * NotificationService
   *
   * 웹소켓 기반 알림 전송 기능을 제공합니다.
   * 구독 경로(Subscribe Endpoint) 규칙:
   *   - /topic/notification         : 전체 사용자 대상 Broadcast 알림
   *   - /user/queue/notification    : 특정 사용자 대상 개인 알림
   *   - /topic/team/{teamId}        : 특정 팀 전체 대상 팀 알림
   *
   * 사용 예시:
   *   - 공지사항/이벤트 시작 → NoticeBroadcast()
   *   - 특정 유저만 초대/경고 알림 → NoticeToMember()
   *   - 팀 과제 완료, 팀 공지 → NoticeToTeam()
   */

  /**
   * 전체 사용자에게 알림을 전송합니다.
   *
   * @param notificationDto  전송할 알림 DTO (title, content 등 포함)
   *
   * 예시:
   *   notificationService.NoticeBroadcast(
   *       new NotificationDto("서버 점검", "오늘 밤 12시 서버 점검 예정")
   *   );
   *
   * 구독 경로:
   *   /topic/notification
   */
  public void NoticeBroadcast(AlertDto notificationDto) {
    simpMessagingTemplate.convertAndSend("/topic/notification", notificationDto.getContent());
    log.info("전체 유저에게 알림 전송 완료");
  }

  /**
   * 특정 사용자(개인)에게 알림을 전송합니다.
   *
   * @param notificationDto  전송할 알림 DTO
   * @param memberId         대상 사용자 ID
   *
   * 예시:
   *   notificationService.NoticeToMember(dto, 5L);
   *
   * 구독 경로:
   *   /user/queue/notification
   */
  public void NoticeToMember(AlertDto notificationDto, Long memberId) {
    simpMessagingTemplate.convertAndSendToUser(memberId.toString(), "/queue/notification", notificationDto.getContent());
    log.info("{}번 유저에게 알림 전송 완료", memberId);
  }

  /**
   * 특정 팀 전체에게 알림을 전송합니다.
   *
   * @param notificationDto  전송할 알림 DTO
   * @param teamId           대상 팀 ID
   *
   * 예시:
   *   notificationService.NoticeToTeam(dto, 10L);
   *
   * 구독 경로:
   *   /topic/team/{teamId}
   */
  public void NoticeToTeam(AlertDto notificationDto, Long teamId) {
    simpMessagingTemplate.convertAndSend("/topic/team/" + teamId, notificationDto.getContent());
    log.info("{}팀에 알림 전송 완료", teamId);
  }
}

