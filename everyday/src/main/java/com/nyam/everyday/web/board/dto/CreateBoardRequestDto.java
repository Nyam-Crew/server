package com.nyam.everyday.web.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateBoardRequestDto {

  @NotBlank(message = "제목은 필수입니다.")
  private String boardTitle;
  @NotBlank(message = "내용은 필수입니다.")
  private String boardContent;
  @NotBlank(message = "게시글 타입은 필수입니다.")
  private String boardType;

}
