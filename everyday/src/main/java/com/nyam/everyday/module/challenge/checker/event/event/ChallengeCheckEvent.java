package com.nyam.everyday.module.challenge.checker.event.event;

import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChallengeCheckEvent {

  private Long memberId;
  private ChallengeTag challengeTag;
  private LocalDate targetDate;
}
