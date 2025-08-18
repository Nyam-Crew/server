package com.nyam.everyday.web.bookmark.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyBookmarkListResponseDto {

  @Schema(description = "게시글 ID")
  private Long boardId;
  @Schema(description = "북마크 ID")
  private Long bookmarkId;
  @Schema(description = "게시글 제목")
  private String boardTitle;
  @Schema(description = "게시글 내용")
  private String boardContent;
  @Schema(description = "게시글 작성자 닉네임")
  private String boardAuthorNickname;
  @Schema(description = "좋아요 수")
  private Long likeCount;
  @Schema(description = "조회수")
  private Long viewCount;
  @Schema(description = "댓글 수")
  private Long commentCount;
  @Schema(description = "게시글 유형", example = "식단")
  private String boardType;
  @Schema(description = "북마크한 날짜")
  private LocalDateTime bookmarkedAt;

}
