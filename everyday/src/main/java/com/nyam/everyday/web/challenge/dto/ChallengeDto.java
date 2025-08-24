package com.nyam.everyday.web.challenge.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChallengeDto {

  private String title;
  private String description;
  private boolean cleared;
  private Long targetCount;
  private Long progressCount;
  private LocalDate startDate;
  private LocalDate endDate;
}
