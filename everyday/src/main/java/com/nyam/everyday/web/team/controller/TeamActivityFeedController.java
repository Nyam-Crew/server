package com.nyam.everyday.web.team.controller;

import com.nyam.everyday.module.team.service.TeamActivityFeedRedisService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.team.dto.TeamActivityFeedItem;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 *
 * 팀 활동 피드 관련 컨트롤러
 *
 * @author : 이지은
 * @fileName : TeamActivityFeedController
 * @since : 25. 8. 13.
 *
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams/activity-feed/{teamId}")
public class TeamActivityFeedController {

    private final TeamActivityFeedRedisService feedService;

    @Operation(summary = "팀 실시간 피드 최근 N개 조회")
    @GetMapping
    public ResponseEntity<List<TeamActivityFeedItem>> listRecent(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "30") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(feedService.listRecent(teamId, userDetails.getId(), size));
    }

    @Operation(summary = "커서 기반 조회(무한스크롤)")
    @GetMapping("/before")
    public ResponseEntity<List<TeamActivityFeedItem>> listBefore(
            @PathVariable Long teamId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant cursor,
            @RequestParam(defaultValue = "30") int size,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(feedService.listBefore(teamId, user.getId(), cursor, size));
    }
}