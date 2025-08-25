package com.nyam.everyday.web.boardComment.controller;

import com.nyam.everyday.module.boardComment.service.BoardCommentService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.boardComment.dto.CommentResponseDto;
import com.nyam.everyday.web.boardComment.dto.CreateCommentRequestDto;
import com.nyam.everyday.web.boardComment.dto.EditCommentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "댓글", description = "게시글 댓글/대댓글")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board-comments")
public class BoardCommentController {

  private final BoardCommentService boardCommentService;

  @Operation(summary = "댓글/대댓글 작성", description = "parentId=null 이면 루트 댓글, 값이 있으면 해당 parent의 대댓글로 저장")
  @PostMapping("/{boardId}")
  public ResponseEntity<CommentResponseDto> createComment(
      @RequestBody @Valid CreateCommentRequestDto dto,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long boardId
  ) {
    if (userDetails == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    CommentResponseDto response = boardCommentService.createComment(boardId, dto, userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "댓글/대댓글 조회", description = "parentId 가 없으면 루트 댓글만, 있으면 해당 parent 의 대댓글만 페이징으로 조회")
  @GetMapping("/{boardId}")
  public ResponseEntity<Page<CommentResponseDto>> getComments(
      @PathVariable Long boardId,
      @RequestParam(required = false) Long parentId,
      @PageableDefault(size = 20, sort = "createdDate", direction = Direction.ASC) Pageable pageable
  ) {
    Page<CommentResponseDto> page = (parentId == null)
        ? boardCommentService.getRootCommentsPage(boardId, pageable)
        : boardCommentService.getRepliesPage(boardId, parentId, pageable);
    return ResponseEntity.ok(page);
  }

  @Operation(summary = "댓글/대댓글 삭제", description = """
      - 권한: 댓글 작성자, 게시글 작성자, 관리자만 가능
      - 정책: 자식(대댓글) 있으면 소프트 삭제, 없으면 하드 삭제
      """)
  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable Long commentId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    if (userDetails == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    boardCommentService.deleteComment(commentId, userDetails.getId());
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "댓글/대댓글 수정", description = "삭제된 댓글은 수정 불가")
  @PatchMapping("/edit/{commentId}")
  public ResponseEntity<CommentResponseDto> editComment(
      @PathVariable Long commentId,
      @Valid @RequestBody EditCommentRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    if (userDetails == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    CommentResponseDto response = boardCommentService.editComment(commentId, userDetails.getId(), request.getContent());
    return ResponseEntity.ok(response);
  }
}