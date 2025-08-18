package com.nyam.everyday.web.notification.controller;

import com.nyam.everyday.module.notification.service.NotificationService;
import com.nyam.everyday.web.notification.dto.NotificationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Notification-Controller", description = "알림을 제공하기 위해 사용하는 테스트 컨트롤러")
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NotificationTestController {

  private final NotificationService notificationService;

  @PostMapping("/broadcast")
  @Operation(summary = "들어온 메세지를 브로드캐스트해주기 위해 사용")
  public ResponseEntity<Void> NotifyBroadcast(@RequestBody NotificationDto notificationDto) {
    notificationService.NoticeBroadcast(notificationDto);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/member/{memberId}")
  @Operation(summary = "들어온 메세지를 특정 유저에게 전송해주기 위해 사용")
  public ResponseEntity<Void> NotifyBroadcast(@RequestBody NotificationDto notificationDto, @PathVariable Long memberId) {
    notificationService.NoticeToMember(notificationDto, memberId);

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/team/{teamId}")
  @Operation(summary = "들어온 메세지를 team 채널에 브로드캐스트")
  public ResponseEntity<Void> NotifyToTeam(@RequestBody NotificationDto notificationDto, @PathVariable Long teamId) {
    notificationService.NoticeToTeam(notificationDto, teamId);

    return ResponseEntity.noContent().build();
  }
}

