package com.nyam.everyday.web.team.controller;

import com.nyam.everyday.module.team.service.TeamNoticeService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.team.dto.TeamNoticeCreatedDto;
import com.nyam.everyday.web.team.dto.TeamNoticeDto;
import com.nyam.everyday.web.team.dto.TeamNoticeUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * 그룹 공지 컨트롤러
 *
 * @author : 이지은
 * @fileName : TeamNoticeController
 * @since : 25. 8. 12.
 *
 */
@Tag(name="TeamNotice-Controller", description = "그룹 공지 CRUD 및 그룹 공지 알림 컨트롤러입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams/notices")
public class TeamNoticeController {

    private final TeamNoticeService teamNoticeService;

    @Operation(summary = "그룹 공지 작성 (방장/부방장 전용), 공지는 그룹당 한건만 가능")
    @PostMapping("/{teamId}")
    public ResponseEntity<TeamNoticeDto> createNotice(
            @PathVariable Long teamId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TeamNoticeCreatedDto req
    ) {
        Long memberId = userDetails.getId(); // 프로젝트에 따라 getId()면 이 줄만 교체
        TeamNoticeDto dto = teamNoticeService.createNotice(teamId, memberId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "그룹 공지 수정(PATCH)", description = "제목/본문 중 전달된 값만 부분 수정. 리더/부리더만 가능.")
    @PatchMapping(value = "/{teamId}/{noticeId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TeamNoticeDto> updateNotice(
            @PathVariable Long teamId,
            @PathVariable Long noticeId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TeamNoticeUpdateDto req
    ) {
        Long memberId = userDetails.getId();
        TeamNoticeDto dto = teamNoticeService.updateNotice(teamId, noticeId, memberId, req);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "그룹 공지 목록 조회")
    @GetMapping("/{teamId}")
    public ResponseEntity<List<TeamNoticeDto>> getNotices( // 반환 타입을 List로 변경
                                                           @PathVariable Long teamId,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();
        // 서비스의 새로운 목록 조회 메소드를 호출
        return ResponseEntity.ok(teamNoticeService.getNoticesByTeam(teamId, memberId));
    }

    @Operation(summary = "그룹 공지 삭제(Hard)", description = "그룹 공지 삭제는 리더/부리더만 가능")
    @DeleteMapping("/{teamId}/{noticeId}")
    public ResponseEntity<Void> deleteNotice(
            @PathVariable Long teamId,
            @PathVariable Long noticeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getId();
        teamNoticeService.deleteNotice(teamId, noticeId, memberId);
        return ResponseEntity.noContent().build();
    }

}