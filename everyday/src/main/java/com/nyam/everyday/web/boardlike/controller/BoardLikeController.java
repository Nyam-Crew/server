package com.nyam.everyday.web.boardlike.controller;

import com.nyam.everyday.module.boardLike.service.BoardLikeService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.boardlike.dto.BoardLikeRequestDto;
import com.nyam.everyday.web.boardlike.dto.BoardLikeResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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


}