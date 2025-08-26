package com.nyam.everyday.module.notification.service;

import com.nyam.everyday.web.notification.dto.NotificationDto;
import com.nyam.everyday.web.notification.dto.NotifyToReactDto;
import com.nyam.everyday.web.team.dto.TeamNotifyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class NotifyToReactService {

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
   * @param notifyToReactDto  전송할 알림 DTO (title, content 등 포함)
   *
   * 예시:
   *   notificationService.NoticeBroadcast(
   *       new NotificationDto("서버 점검", "오늘 밤 12시 서버 점검 예정")
   *   );
   *
   * 구독 경로:
   *   /topic/notification
   */
  public void NotifyBroadcast(NotifyToReactDto notifyToReactDto) {
    simpMessagingTemplate.convertAndSend("/topic/notification", notifyToReactDto);
    log.info("전체 유저에게 알림 전송 완료");
  }

  /**
   * 특정 사용자(개인)에게 알림을 전송합니다.
   *
   * @param notifyToReactDto  전송할 알림 DTO
   * @param memberId         대상 사용자 ID
   *
   * 예시:
   *   notificationService.NoticeToMember(dto, 5L);
   *
   * 구독 경로:
   *   /user/queue/notification
   */
  public void NotifyToMember(NotifyToReactDto notifyToReactDto, Long memberId) {
    simpMessagingTemplate.convertAndSendToUser(memberId.toString(), "/queue/notification", notifyToReactDto);
    log.info("{}번 유저에게 알림 전송 완료", memberId);
  }

  /**
   * 특정 팀 전체에게 알림을 전송합니다.
   *
   * @param teamNotifyDto  전송할 알림 DTO
   * @param teamId           대상 팀 ID
   *
   * 예시:
   *   notificationService.NoticeToTeam(dto, 10L);
   *
   * 구독 경로:
   *   /topic/team/{teamId}
   */
  public void NotifyToTeam(TeamNotifyDto teamNotifyDto, Long teamId) {
    simpMessagingTemplate.convertAndSend("/topic/team/" + teamId, teamNotifyDto);
    log.info("{}팀에 알림 전송 완료", teamId);
  }

  /**
   * [신규] 특정 사용자(개인)에게 '팀 알림'을 전송합니다. (메소드 오버로딩)
   * TeamNotifyDto를 받아서 공용 Dto로 변환 후 전송합니다.
   *
   * @param teamNotifyDto    전송할 팀 알림 DTO
   * @param memberId         대상 사용자 ID
   */
  public void NotifyToMember(TeamNotifyDto teamNotifyDto, Long memberId) {
    // 1. 내부에서 공용 DTO인 NotifyToReactDto로 변환합니다.
    NotifyToReactDto generalDto = NotifyToReactDto.builder()
            .content(teamNotifyDto.getContent())
            .createdAt(teamNotifyDto.getCreatedAt())
            .build();

    // 2. 기존 NotifyToMember 메소드를 호출합니다.
    this.NotifyToMember(generalDto, memberId);
  }

}

