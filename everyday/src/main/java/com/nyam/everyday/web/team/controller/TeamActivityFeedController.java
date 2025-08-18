package com.nyam.everyday.web.team.controller;

import com.nyam.everyday.module.team.service.TeamActivityFeedService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.team.dto.FeedSlice;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    private final TeamActivityFeedService feedService;

    /**
     * 초기 로드(최신 페이지) — FeedSlice로 반환
     * nextCursorEpochMs는 이번 페이지의 마지막 아이템 score(=createdAtMs)
     */
    @Operation(summary = "팀 실시간 피드 최신 페이지 조회(초기 로드)")
    @GetMapping
    public ResponseEntity<FeedSlice> getLatestPage(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "30") int size,
            @AuthenticationPrincipal CustomUserDetails user // 권한 확인이 필요하면 여기서 처리
    ) {
        // 초기 페이지는 cursor=null 로 before API 사용
        FeedSlice slice = feedService.listFeedBefore(teamId, null, size);
        return ResponseEntity.ok(slice);
    }

    /**
     * 아래로 더 보기(무한스크롤) — cursor(미포함) 이전 구간을 최신순으로 반환
     */
    @Operation(summary = "커서 기반 조회(무한스크롤: 아래로 더 보기)")
    @GetMapping("/before")
    public ResponseEntity<FeedSlice> getBefore(
            @PathVariable Long teamId,
            @RequestParam(name = "cursor", required = false) Long cursorEpochMs,
            @RequestParam(defaultValue = "30") int size,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        FeedSlice slice = feedService.listFeedBefore(teamId, cursorEpochMs, size);
        return ResponseEntity.ok(slice);
    }

    /**
     * 위로 당겨 새로고침 — cursor(미포함) 이후의 신규 구간을 최신순으로 반환
     */
    @Operation(summary = "커서 기반 조회(당겨서 새로고침: 위로 신규만)")
    @GetMapping("/after")
    public ResponseEntity<FeedSlice> getAfter(
            @PathVariable Long teamId,
            @RequestParam(name = "cursor") Long cursorEpochMs,
            @RequestParam(defaultValue = "30") int size,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        FeedSlice slice = feedService.listFeedAfter(teamId, cursorEpochMs, size);
        return ResponseEntity.ok(slice);
    }
}