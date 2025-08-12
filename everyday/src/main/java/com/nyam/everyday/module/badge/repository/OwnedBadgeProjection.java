package com.nyam.everyday.module.badge.repository;

import java.time.LocalDateTime;

public interface OwnedBadgeProjection {
  Long getBadgeId();
  LocalDateTime getAcquiredAt(); // = createdDate
}