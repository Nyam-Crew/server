package com.nyam.everyday.web.challenge.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegularChallengeDto {

  private Long challengeId;
  private String title;
  private String description;
  private boolean cleared;
  private Long targetCount;
  private Long progressCount;
}
