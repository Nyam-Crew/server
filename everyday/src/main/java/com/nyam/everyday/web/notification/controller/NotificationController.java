package com.nyam.everyday.web.notification.controller;

import com.nyam.everyday.module.notification.service.NotificationService;
import com.nyam.everyday.web.notification.dto.NotificationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Notification-Controller", description = "알림을 제공하기 위해 사용하는 테스트 컨트롤러")
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @PostMapping("/broadcast")
  @Operation(summary = "들어온 메세지를 브로드캐스트해주기 위해 사용")
  public ResponseEntity<Void> BroadcastContent(@RequestBody NotificationDto notificationDto) {
    notificationService.BroadcastContent(notificationDto);

    return ResponseEntity.noContent().build();
  }
}

