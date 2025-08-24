package com.nyam.everyday.web.team.controller;

import com.nyam.everyday.module.chatting.chatmessage.service.ChatMessageService;
import com.nyam.everyday.module.team.service.TeamNotificationService;
import com.nyam.everyday.web.chatting.dto.ChatMessageSaveRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 팀 알림 테스트 API
 *
 * @author : 이지은
 * @fileName : TeamNotificationTestController
 * @since : 25. 8. 22.
 */
@Tag(name="Team Notification Test", description = "그룹 알림 테스트용 API 입니다.")
@RestController
@RequestMapping("/test/team") // 엔드포인트는 /test 처럼 명확히 구분되게 합니다.
@RequiredArgsConstructor
public class TestTeamNotificationController {

    private final TeamNotificationService teamNotificationService;
    private final ChatMessageService chatMessageService;

    @Operation(summary = "요약 알림 테스트", description = "피드를 생성할 수 있는 기록을 여러개 추가 후 해당 로직을 실행하면 요약 알림 전송을 db에서 확인해야합니다.")
    @GetMapping("/trigger-summary-notification")
    public String triggerSummaryNotification() {
        teamNotificationService.sendAggregatedNotifications();
        return "OK. Summary notification process has been triggered.";
    }

    @PostMapping("/send-chat/{teamId}")
    public ResponseEntity<String> sendTestChatMessage(
            @PathVariable Long teamId,
            @RequestParam String message,
            @RequestParam Long senderId // 실제로는 @AuthenticationPrincipal로 가져와야 하지만, 테스트 편의상 파라미터로 받음
    ) {
        // 실제 컨트롤러가 하던 DTO 생성을 흉내 냅니다.
        ChatMessageSaveRequest request = ChatMessageSaveRequest.builder()
                .content(message)
                .build();
        // request에 nickname 등 다른 필요 필드가 있다면 추가해 주세요.

        // 실제 로직이 담긴 '서비스' 메서드를 직접 호출합니다.
        chatMessageService.handleMessage(request, senderId, teamId);

        return ResponseEntity.ok("Test chat message sent and notification process triggered for teamId: " + teamId);
    }
}
