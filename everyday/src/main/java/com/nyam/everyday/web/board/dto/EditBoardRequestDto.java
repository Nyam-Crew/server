package com.nyam.everyday.web.board.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "EditBoardRequestDto",description = "게시글 수정 요청, null인 필드는 변경하지 않음")
public class EditBoardRequestDto {

  @Schema(description = "수정할 제목",example = "제목만 살짝 수정")
  private String title;

  @Schema(description = "수정할 내용")
  private String content;

}
