package com.nyam.everyday.module.bookmark.dto;

import com.nyam.everyday.module.board.entity.Board;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BookmarkAndBoardDto {

  private Long boardId;
  private Long bookmarkId;
  private String boardTitle;
  private String boardContent;
  private String boardAuthorNickname;
  private Long likeCount;
  private Long viewCount;
  private Long commentCount;
  private String boardType;
  private LocalDateTime bookmarkedAt; // 북마크한 날짜

  public BookmarkAndBoardDto(Board board, Long bookmarkId, LocalDateTime bookmarkedAt) {
    this.bookmarkId = bookmarkId;
    this.boardId = board.getBoardId();
    this.boardTitle = board.getBoardTitle();
    this.boardContent = board.getBoardContent();
    this.boardAuthorNickname = board.getMember().getNickname();
    this.likeCount = board.getLikeCount();
    this.viewCount = board.getViewCount();
    this.commentCount = board.getCommentCount();
    this.boardType = board.getBoardType();
    this.bookmarkedAt = bookmarkedAt;
  }
}