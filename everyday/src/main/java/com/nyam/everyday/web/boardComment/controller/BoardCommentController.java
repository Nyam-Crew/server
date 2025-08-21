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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "댓글", description = "게시글 댓글/대댓글")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board-comments")
public class BoardCommentController {

  private final BoardCommentService boardCommentService;

  @Operation(summary = "댓글/대댓글 작성",description = "parentId = null일 경우 댓글,값이 있을 경우 대댓글로 저장")
  @PostMapping("/{boardId}")
  public ResponseEntity<CommentResponseDto> createComment(@RequestBody @Valid CreateCommentRequestDto dto,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long boardId
      ) {
    // 1.인증 가드 : 토큰 파싱 실패 등으로 null이면 401
    if (userDetails == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // 2) 서비스 호출: 내부에서 member/board 조회, parent 검증(같은 보드/깊이 제한), 저장, 카운트 증가까지 수행
    CommentResponseDto response = boardCommentService.createComment(boardId, dto,userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "댓글/대댓글 조회",description = "특정 게시글의 댓글/대댓글을 페이징으로 조회합니다.")
  @GetMapping("/{boardId}")
  public ResponseEntity<Page<CommentResponseDto>> getComments(
      @PathVariable Long boardId,
      @PageableDefault(
          size = 20,
          sort = "createdDate",
          direction = Direction.ASC
      )Pageable pageable
  ){
        Page<CommentResponseDto> page = boardCommentService.getCommentsPage(boardId,pageable);

        return ResponseEntity.ok(page);
  }


  @Operation(summary = "댓글/대댓글 삭제",description = """
      -권한: 댓글 작성자, 게시글 작성자, 관리자만 가능
      -정책: 자식(대댓글) 있으면 소프트 삭제, 없으면 하드 삭제
      -응답: 204 NoContent
      """)
  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(@PathVariable Long commentId,
          @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long requesterId = userDetails.getId();

    boardCommentService.deleteComment(commentId, requesterId);
    return ResponseEntity.noContent().build();

  }

  @Operation(summary = "댓글 수정",description = "댓글 및 대댓글 수정이 진행 되며 삭제 된 댓글은 수정 불가능")
  @PatchMapping("/edit/{commentId}")
  public ResponseEntity<CommentResponseDto> editComment(

      @PathVariable Long commentId,
      @Valid @RequestBody EditCommentRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ){
    Long requesterId = userDetails.getId();
    CommentResponseDto response = boardCommentService.editComment(
        commentId,requesterId,request.getContent());
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

}
