package com.nyam.everyday.web.boardComment.controller;



import com.nyam.everyday.module.boardComment.service.BoardCommentService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.boardComment.dto.CreateCommentRequestDto;
import com.nyam.everyday.web.boardComment.dto.CreateCommentResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board-comments")
public class BoardCommentController {

  private final BoardCommentService boardCommentService;

  @PostMapping("/{boardId}")
  public ResponseEntity<CreateCommentResponseDto> createComment(@RequestBody @Valid CreateCommentRequestDto dto,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long boardId
      ) {
    CreateCommentResponseDto response = boardCommentService.createComment(boardId, dto,userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

}
