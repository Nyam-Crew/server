package com.nyam.everyday.web.badge.controller;

import com.nyam.everyday.module.badge.service.BadgeService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.badge.dto.BadgeDto;
import com.nyam.everyday.web.badge.dto.BadgeOwnershipDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Badge-Controller", description = "뱃지 관리")
@RestController
@RequestMapping("/api/badge")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @PostMapping
    @Operation(summary = "뱃지 생성", description = "새로운 뱃지를 생성합니다.")
    public ResponseEntity<BadgeDto> createBadge (@RequestBody BadgeDto badgeDto) {
        return ResponseEntity.ok(badgeService.createBadge(badgeDto));
    }

    @DeleteMapping("/{badgeId}")
    @Operation(summary = "뱃지 삭제", description = "지정된 ID의 뱃지를 삭제합니다.")
    public ResponseEntity<Void> deleteBadge (@PathVariable Long badgeId) {
        log.info("[deleteBadge] badgeId: {}", badgeId);
        badgeService.deleteBadge(badgeId);
        return ResponseEntity.ok().build();
    }


    /**
     * 페이징 입력 형식
     * {
     *   "page": 0,
     *   "size": 9,
     *   "sort":
     *     "createdDate"
     * }
     * */
    @GetMapping("/my-badges")
    @Operation(summary = "뱃지 목록 조회 (페이지네이션)", description = "페이지네이션된 뱃지 목록을 조회합니다. 현재 사용자의 소유 여부도 포함됩니다.")
    public ResponseEntity<Page<BadgeOwnershipDto>> getBadges(
        @PageableDefault(size = 9, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
        @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("[getBadges] Pageable request: {}", pageable);

        Long currentUserId = (userDetails != null) ? userDetails.getId() : null;
        log.info("[getBadges] User ID: {}", currentUserId);

        Page<BadgeOwnershipDto> response = badgeService.getBadgeListWithOwnership(pageable, currentUserId);
        return ResponseEntity.ok(response);
    }
}

