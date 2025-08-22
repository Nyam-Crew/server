package com.nyam.everyday.web.team.controller;

import com.nyam.everyday.module.team.service.TeamNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 팀 요약 알림 테스트 API
 *
 * @author : 이지은
 * @fileName : TeamNotificationTestController
 * @since : 25. 8. 22.
 */
@Tag(name="Team Test", description = "그룹의 전반적인 기본 흐름을 확인할 수있는 테스트용 API 입니다.")
@RestController
@RequestMapping("/test/team") // 엔드포인트는 /test 처럼 명확히 구분되게 합니다.
@RequiredArgsConstructor
public class TeamNotificationTestController {

    private final TeamNotificationService teamNotificationService;

    @Operation(summary = "요약 알림 테스트", description = "피드를 생성할 수 있는 기록을 여러개 추가 후 해당 로직을 실행하면 요약 알림 전송을 db에서 확인해야합니다.")
    @GetMapping("/trigger-summary-notification")
    public String triggerSummaryNotification() {
        teamNotificationService.sendAggregatedNotifications();
        return "OK. Summary notification process has been triggered.";
    }
}