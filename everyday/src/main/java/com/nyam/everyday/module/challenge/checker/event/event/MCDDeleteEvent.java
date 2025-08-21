package com.nyam.everyday.module.challenge.checker.event.event;

import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.member.entity.Member;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MCDDeleteEvent {

  private Member member;
  private Challenge challenge;
  private LocalDate targetDate;

}
