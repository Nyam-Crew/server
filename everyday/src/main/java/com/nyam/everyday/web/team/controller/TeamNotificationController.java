package com.nyam.everyday.web.team.controller;

import com.nyam.everyday.module.team.enums.TeamNotificationType;
import com.nyam.everyday.module.team.service.TeamNotificationService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.notification.dto.NotificationStatusDto;
import com.nyam.everyday.web.team.dto.TeamNotificationBoxDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * 그룹 알림 관리 컨트롤러
 *
 * @author : 이지은
 * @fileName : TeamNotificationController
 * @since : 25. 8. 20.
 *
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams/notification")
public class TeamNotificationController {

    private final TeamNotificationService teamNotificationService;

    @Operation(summary = "사용자에게 전송된 알림 목록을 20개까지 불러옵니다.")
    @GetMapping("/history")
    public ResponseEntity<List<TeamNotificationBoxDto>> getTeamNotifications(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long memberId = customUserDetails.getId();

        List<TeamNotificationBoxDto> result = teamNotificationService.getTeamNotifications(memberId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "새로운 알림이 있는지 판별합니다. isChecked=false가 1개라도 있으면 true")
    @GetMapping("/status")
    public NotificationStatusDto hasNewTeam(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return teamNotificationService.hasNewTeamNotifications(userDetails.getId());
    }

    @Operation(summary = "알림함을 열었을 때: 해당 사용자의 모든 미읽음 알림을 읽음 처리합니다.")
    @PostMapping("/inbox/opened")
    public ResponseEntity<Integer> markInboxOpened(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        int updated = teamNotificationService.markTeamInboxOpened(user.getId());
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "특정 페이지(채팅/피드/공지) 진입 시: 해당 팀의 해당 타입 알림과 SUMMARY를 읽음 처리합니다.")
    @PutMapping("/read-by-context")
    public ResponseEntity<Integer> markReadByContext(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam Long teamId,
            @RequestParam TeamNotificationType type
    ) {
        int updated = teamNotificationService.markPageOpened(user.getId(), teamId, type);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "단건 알림 읽음 처리(푸시/딥링크 진입 시 사용).")
    @PostMapping("/{notificationId}/check")
    public ResponseEntity<Void> markOneChecked(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long notificationId
    ) {
        teamNotificationService.markOneTeamNotificationChecked(user.getId(), notificationId);
        return ResponseEntity.ok().build();
    }
}