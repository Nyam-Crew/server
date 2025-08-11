package com.nyam.everyday.web.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CreateBookmarkResponseDto {
  private Long bookmarkId;
  private Long boardId;
  private Long memberId;
  private boolean bookmarked; //항상 true
}
