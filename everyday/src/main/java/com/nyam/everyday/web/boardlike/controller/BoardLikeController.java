package com.nyam.everyday.web.boardlike.controller;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.boardLike.service.BoardLikeService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.boardlike.dto.BoardLikeRequestDto;
import com.nyam.everyday.web.boardlike.dto.BoardLikeResponseDto;
import com.nyam.everyday.web.boardlike.dto.LikeStatusResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "게시글 좋아요 관리", description = "게시글 좋아요 관련 API")
@RestController
@RequestMapping("/api/board-likes")
@RequiredArgsConstructor
public class BoardLikeController {

    private final BoardLikeService boardLikeService;

    @Operation(summary = "게시글 좋아요", description = "특정 게시글에 좋아요를 누릅니다.")
    @PostMapping("/toggle")
    public ResponseEntity<BoardLikeResponseDto> toggleLike(
            @Valid @RequestBody BoardLikeRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();
        BoardLikeResponseDto response = boardLikeService.toggleBoardLike(dto.getBoardId(), memberId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // === 좋아요 상태 일괄 조회 ===
    // 프런트: GET /api/board-likes/status?boardIds=1&boardIds=2&boardIds=3
    // 응답: { "liked": [1,3] }
    @GetMapping("/status")
    public ResponseEntity<LikeStatusResponseDto> status(
        @RequestParam("boardIds") List<Long> boardIds,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getId();

        LikeStatusResponseDto dto = boardLikeService.status(boardIds, memberId);
        return ResponseEntity.ok(dto);
    }
    @Getter
    @Setter
    public static class ToggleLikeRequest {
        @NotNull
        private Long boardId;
    }
}