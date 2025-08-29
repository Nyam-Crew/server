package com.nyam.everyday.web.boardlike.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ToggleLikeResponseDto {
  private Long totalLikes;   // 해당 글의 총 좋아요 수
  private boolean isLiked;  // 내가 좋아요한 상태인지

}
