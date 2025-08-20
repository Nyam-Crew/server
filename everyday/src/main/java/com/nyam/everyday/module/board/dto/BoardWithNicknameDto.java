package com.nyam.everyday.module.board.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BoardWithNicknameDto {

  private Long boardId;
  private String boardTitle;
  private String nickname;
  private String boardType;
  private LocalDateTime createdDate;
  private Long viewCount;
  private Long commentCount;
  private Long likeCount;

  public BoardWithNicknameDto(Long boardId, String boardTitle, String nickname, String boardType,
      LocalDateTime createdDate, Long viewCount, Long commentCount, Long likeCount) {
    this.boardId = boardId;
    this.boardTitle = boardTitle;
    this.nickname = nickname;
    this.boardType = boardType;
    this.createdDate = createdDate;
    this.viewCount = viewCount;
    this.commentCount = commentCount;
    this.likeCount = likeCount;
  }
}
