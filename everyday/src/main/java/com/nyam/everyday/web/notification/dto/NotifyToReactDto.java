package com.nyam.everyday.web.notification.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotifyToReactDto {
  private String content;
  private LocalDateTime createdAt;
}
