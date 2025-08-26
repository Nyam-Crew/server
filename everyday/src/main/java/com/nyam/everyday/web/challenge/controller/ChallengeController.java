package com.nyam.everyday.web.challenge.controller;

import com.nyam.everyday.module.challenge.service.ChallengeService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.challenge.dto.EventAttendDto;
import com.nyam.everyday.web.challenge.dto.EventChallengeDto;
import com.nyam.everyday.web.challenge.dto.RegularChallengeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/challenge")
@RequiredArgsConstructor
@Tag(name = "Challenge-Controller", description = "챌린지 관련 기능을 수행하는 컨트롤러입니다.")
public class ChallengeController {

  private final ChallengeService challengeService;

  @GetMapping("/progressing")
  @Operation(summary = "메인 페이지에 표시될 \"진행중인 챌린지\"의 목록을 2개까지 반환합니다.")
  public ResponseEntity<List<RegularChallengeDto>> getProgressingChallenge(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long memberId = customUserDetails.getId();

    List<RegularChallengeDto> result = challengeService.getProgressingChallenge(memberId);

    return ResponseEntity.ok(result);
  }

  @GetMapping("/regular")
  @Operation(summary = "챌린지 페이지의 \"상시 챌린지\"에 표시될 목록을 반환합니다.")
  public ResponseEntity<List<RegularChallengeDto>> getRegularChallengeList(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long memberId = customUserDetails.getId();

    List<RegularChallengeDto> result = challengeService.getRegularChallengeList(memberId);

    return ResponseEntity.ok(result);
  }

  @GetMapping("/event")
  @Operation(summary = "챌린지 페이지의 \"이벤트 챌린지\"에 표시될 목록을 반환합니다.")
  public ResponseEntity<List<EventChallengeDto>> getEventChallengeList(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long memberId = customUserDetails.getId();

    List<EventChallengeDto> result = challengeService.getEventChallengeList(memberId);

    return ResponseEntity.ok(result);
  }

  @PostMapping("/event")
  @Operation(summary = "이벤트 챌린지에 참여 처리합니다.")
  public ResponseEntity<?> attendToEventChallenge(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestBody EventAttendDto dto
  ) {
    Long memberId = customUserDetails.getId();
    challengeService.attendToEventChallenge(memberId, dto);

    return ResponseEntity.noContent().build();
  }
}
