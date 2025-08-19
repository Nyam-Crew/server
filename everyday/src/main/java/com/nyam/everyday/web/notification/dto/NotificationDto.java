package com.nyam.everyday.web.notification.dto;

import com.nyam.everyday.module.notification.entity.Notification;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationDto {
  private String content;
  private LocalDateTime createdAt;
  private Boolean isRead;
}
