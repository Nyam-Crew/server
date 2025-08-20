package com.nyam.everyday.web.badge.controller;

import static org.springframework.http.HttpStatus.CREATED;

import com.nyam.everyday.common.util.FileValidationUtils;
import com.nyam.everyday.module.badge.service.BadgeService;
import com.nyam.everyday.web.badge.dto.BadgeCreateRequestDto;
import com.nyam.everyday.web.badge.dto.BadgeResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Tag(name = "Badge-Controller", description = "뱃지 관리")
@RestController
@RequestMapping("/api/badge")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "뱃지 생성",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                encoding = {
                    @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE),
                    @Encoding(name = "file", contentType = "image/png, image/jpeg")
                }
            )
        )
    )
    public ResponseEntity<BadgeResponseDto> createBadge(
        @RequestPart("request") @jakarta.validation.Valid BadgeCreateRequestDto request,
        @RequestPart(value = "file", required = false) MultipartFile file) {

        FileValidationUtils.validateOptionalPngJpeg(file, 5 * 1024 * 1024L); // 5MB 제한
        BadgeResponseDto created = badgeService.createBadge(request, file);
        return ResponseEntity.status(CREATED).body(created);
    }

    @DeleteMapping("/{badgeId}")
    @Operation(summary = "뱃지 삭제", description = "지정된 ID의 뱃지를 삭제합니다.")
    public ResponseEntity<Void> deleteBadge(@PathVariable Long badgeId) {
        log.info("[deleteBadge] badgeId: {}", badgeId);
        badgeService.deleteBadge(badgeId);
        return ResponseEntity.ok().build();
    }
}

