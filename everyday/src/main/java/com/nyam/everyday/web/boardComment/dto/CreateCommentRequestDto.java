package com.nyam.everyday.web.boardComment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCommentRequestDto {

  @NotBlank(message = "댓글 내용은 비어 있을 수 없습니다")
  private String content;

}
