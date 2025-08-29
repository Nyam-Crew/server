package com.nyam.everyday.web.boardlike.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ToggleLikeRequestDto {
  @Schema(description = "좋아요 진행 게시글")
  private Long boardId;

}
