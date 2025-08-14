package com.nyam.everyday.web.badge.controller;

import com.nyam.everyday.module.badge.service.BadgeService;
import com.nyam.everyday.web.badge.dto.BadgeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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


}

