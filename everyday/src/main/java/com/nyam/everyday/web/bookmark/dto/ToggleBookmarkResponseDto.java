package com.nyam.everyday.web.bookmark.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ToggleBookmarkResponseDto {
  @Schema(description = "게시글 ID")
  private Long boardId;
  @Schema(description = "북마크 여부")
  private boolean bookmarked;

}
