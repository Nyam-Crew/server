package com.nyam.everyday.web.notification.controller;

import com.nyam.everyday.module.notification.entity.NotificationType;
import com.nyam.everyday.module.notification.service.NotificationService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.notification.dto.NotificationDto;
import com.nyam.everyday.web.notification.dto.NotificationStatusDto;
import com.nyam.everyday.web.notification.dto.NotificationTestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Notification-Controller", description = "알림을 제공하기 위해 사용하는 컨트롤러입니다")
@RequestMapping("/api/notify")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping("/history")
  @Operation(summary = "사용자에게 전송된 알림 목록을 20개까지 불러옵니다.")
  public ResponseEntity<List<NotificationDto>> getNotifications(@AuthenticationPrincipal
  CustomUserDetails customUserDetails) {
    Long memberId = customUserDetails.getId();

    List<NotificationDto> result = notificationService.getNotifications(memberId);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/status")
  @Operation(summary = "사용자가 확인할 새 알림이 있는지를 true, false로 확인합니다.")
  public ResponseEntity<NotificationStatusDto> hasNewNotifications(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long memberId = customUserDetails.getId();

    NotificationStatusDto result = notificationService.hasNewNotifications(memberId);
    return ResponseEntity.ok(result);
  }



  /// 이 아래는 테스트용 엔드포인트입니다 ///



  @PostMapping("/broadcast")
  @Operation(summary = "@테스트 Broadcast 알림을 생성하고, 사용자에게 토스트 메세지를 전달합니다.")
  public ResponseEntity<Void> NotifyBroadcast(@RequestBody NotificationTestDto notificationTestDto) {
    notificationService.addBroadcastNotification(notificationTestDto.getContent(), NotificationType.BROADCAST);

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/member/{memberId}")
  @Operation(summary = "@테스트 개인 알림을 생성하고, 사용자에게 토스트 메세지를 전달합니다.")
  public ResponseEntity<Void> NotifyBroadcast(@RequestBody NotificationTestDto notificationTestDto, @PathVariable Long memberId) {
    notificationService.addPrivateNotification(notificationTestDto.getContent(), memberId, NotificationType.BROADCAST);

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/team/{teamId}")
  @Operation(summary = "@테스트 팀 알림을 생성하고, 사용자에게 토스트 메세지를 전달합니다. 지은님이 다시 구현하실 예정입니다(팀 알림 담당)")
  public ResponseEntity<Void> NotifyToTeam(@RequestBody NotificationTestDto notificationTestDto, @PathVariable Long teamId) {
    notificationService.addTeamNotification(notificationTestDto.getContent(), teamId, NotificationType.TEAM);

    return ResponseEntity.noContent().build();
  }
}