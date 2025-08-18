package com.nyam.everyday.web.board.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoardPageDto {

  private Long boardId;
  private String boardTitle;
  private String nickname;
  private String boardType;
  private LocalDateTime createdDate;
  private Long viewCount;
  private Long commentCount;
  private Long likeCount;

}
