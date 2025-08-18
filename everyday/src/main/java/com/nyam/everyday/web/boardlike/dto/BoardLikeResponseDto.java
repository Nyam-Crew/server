package com.nyam.everyday.web.boardlike.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class BoardLikeResponseDto {

  private Long likeId;
  private Long memberId;
  private Long boardId;
  private boolean liked;
  private long likeCount;
}
