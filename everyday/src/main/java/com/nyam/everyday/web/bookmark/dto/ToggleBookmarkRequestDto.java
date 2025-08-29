package com.nyam.everyday.web.bookmark.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ToggleBookmarkRequestDto {
  @Schema(description = "게시글 ID")
  private Long boardId;

}
