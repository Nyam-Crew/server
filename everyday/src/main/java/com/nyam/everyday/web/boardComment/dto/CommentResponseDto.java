package com.nyam.everyday.web.boardComment.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDto {

  private Long commentId;
  private Long boardId;
  private Long memberId;
  private String content;
  private String nickname;
  private Long parentId;
  private LocalDateTime createdDate;
  private LocalDateTime modifiedDate;
  private boolean edited;


}
