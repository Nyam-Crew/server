package com.nyam.everyday.web.board.controller;

import com.nyam.everyday.module.board.service.BoardService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.board.dto.BoardResponseDto;
import com.nyam.everyday.web.board.dto.CreateBoardRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boards")
@Slf4j
@RequiredArgsConstructor
public class BoardController {

  private final BoardService boardService;

  @Operation(summary = "게시글 작성",description = "게시판에 게시글을 작성합니다")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "게시글 작성 성공"),
      @ApiResponse(responseCode = "404",description = "사용자 정보 없음"),
      @ApiResponse(responseCode = "400",description = "잘못된 요청 방식")
  })
  @PostMapping("/post")
  public ResponseEntity<BoardResponseDto> createBoard(@Valid @RequestBody CreateBoardRequestDto dto
      ,@AuthenticationPrincipal CustomUserDetails userDetails) {
    BoardResponseDto response = boardService.createBoard(dto, userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

}
