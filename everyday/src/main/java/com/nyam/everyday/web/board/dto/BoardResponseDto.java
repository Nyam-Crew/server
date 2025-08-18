package com.nyam.everyday.web.board.dto;

import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.module.member.entity.Member;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardResponseDto {


  private Long boardId;
  private String nickname;
  private String boardTitle;
  private String boardType;
  private LocalDateTime createdDate;





  public static BoardResponseDto toDto(Board entity){
    return BoardResponseDto.builder()
        .boardId(entity.getBoardId())
        .nickname(entity.getMember().getNickname())
        .boardTitle(entity.getBoardTitle())
        .boardType(entity.getBoardType())
        .createdDate(entity.getCreatedDate())
        .build();
  }

}
