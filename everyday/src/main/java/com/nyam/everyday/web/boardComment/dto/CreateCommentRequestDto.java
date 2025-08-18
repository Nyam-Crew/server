package com.nyam.everyday.web.boardComment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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

  @Positive(message = "parentid는 양수여야합니다")
  private Long parentId;

}
