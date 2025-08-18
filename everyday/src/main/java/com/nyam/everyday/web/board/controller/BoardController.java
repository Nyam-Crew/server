package com.nyam.everyday.web.board.controller;

import com.nyam.everyday.module.board.service.BoardService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.board.dto.BoardPageDto;
import com.nyam.everyday.web.board.dto.BoardResponseDto;
import com.nyam.everyday.web.board.dto.CreateBoardRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  @Operation(summary = "게시글 삭제",description = "사용자가 본인의 게시판을 삭제합니다")
  @ApiResponses(value ={
      @ApiResponse(responseCode = "204", description = "게시글이 삭제 되었습니다"),
      @ApiResponse(responseCode = "404", description = "이미 삭제되었거나 존재하지 않는 게시글입니다")
  })
  @DeleteMapping("/delete/{boardId}")
  public ResponseEntity<Void> deleteBoard(
              @AuthenticationPrincipal CustomUserDetails customUserDetails,
              @PathVariable Long boardId){
    Long memberId = customUserDetails.getId();
    boardService.deleteBoard(boardId, memberId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @Operation(summary = "게시글 상세 조회",description = "사용자가 게시글을 조회하고 조회수가 1 증가합니다")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",description = "게시글 조회 성공"),
      @ApiResponse(responseCode = "404",description = "게시글을 찾을 수 없습니다"),
      @ApiResponse(responseCode = "500",description = "서버 에러")
  })
  @GetMapping("/get-detail/{boardId}")
  public ResponseEntity<BoardResponseDto> getBoard(@PathVariable Long boardId) {
    // 1.서비스에서 게시글 조회 및 조회수 증가 처리
   BoardResponseDto res =  boardService.getBoard(boardId);

    return ResponseEntity.status(HttpStatus.OK).body(res);
  }

  @Operation(summary = "게시글 페이징",description = "게시글을 페이징 처리 합니다")
  @GetMapping
  public ResponseEntity<Page<BoardPageDto>> getBoards(
      @RequestParam(required = false) String boardType,
      @PageableDefault(size = 10, sort = "createdDate", direction = Direction.DESC)  Pageable pageable){
    Page<BoardPageDto> boardPage = boardService.getBoardPreviews(boardType, pageable);
    return ResponseEntity.ok(boardPage);
  }

}
