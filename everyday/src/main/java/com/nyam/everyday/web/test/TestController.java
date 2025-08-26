package com.nyam.everyday.web.test;

import com.nyam.everyday.module.chatting.chatmessage.service.ChatMessageService;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.service.MemberService;
import com.nyam.everyday.module.scorelog.entity.SourceType;
import com.nyam.everyday.module.scorelog.service.ScoreLogService;
import com.nyam.everyday.security.core.CustomUserDetails;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 부하 테스트 전용 컨트롤러. local, dev 프로필에서만 활성화됩니다. (운영 환경에서는 비활성화)
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class TestController {

  private final ScoreLogService scoreLogService;
  private final MemberService memberService;
  private final ChatMessageService chatMessageService;

  @PostMapping("/scores")
  public ResponseEntity<Void> addScoreForLoadTest(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody ScoreRequestDto requestDto) {
    Long id = userDetails.getId();
    Member member = memberService.getMemberByMemberId(userDetails.getId());
    log.info("[getMember] memberId : {}", id);
    // k6 스크립트에서 보낸 점수와 이유를 바탕으로 ScoreLog를 생성합니다.
    scoreLogService.createScoreLog(
        member,
        requestDto.getScore(),
        SourceType.MEAL_INPUT
    );

    return ResponseEntity.ok().build();
  }

  @Data
  public static class ScoreRequestDto {

    private Long score;
    private String reason;
  }

  @DeleteMapping("/chat/{teamId}")
  public ResponseEntity<Void> removeTeamChatting(@PathVariable(name = "teamId") Long teamId) {
    chatMessageService.deleteMessagesByTeamId(teamId);

    return ResponseEntity.noContent().build();
  }
}
