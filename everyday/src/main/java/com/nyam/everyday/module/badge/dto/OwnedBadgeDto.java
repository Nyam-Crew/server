package com.nyam.everyday.module.badge.dto;

import java.time.LocalDateTime;

public interface OwnedBadgeDto {
  Long getBadgeId();
  LocalDateTime getAcquiredAt(); // = createdDate
}