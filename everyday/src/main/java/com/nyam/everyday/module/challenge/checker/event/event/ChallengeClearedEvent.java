package com.nyam.everyday.module.challenge.checker.event.event;

import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChallengeClearedEvent {

  private Member member;
  private Challenge challenge;
}
