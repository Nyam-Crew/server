package com.nyam.everyday.web.boardlike.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeStatusResponseDto {
  private List<Long> liked; // 내가 좋아요한 boardId 목록


}
