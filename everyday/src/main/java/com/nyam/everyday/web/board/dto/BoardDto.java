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
public class BoardDto {


  private Long boardId;
  private Member member;
  private String boardTitle;
  private String boardComment;
  private Long viewCount;
  private Long likeCount;
  private Long commentCount;
  private String boardType;
  private LocalDateTime createdDate;
  private LocalDateTime modifiedDate;


  public Board toEntity(Board board, Member member){
    return Board.builder()
        .boardId(boardId)
        .member(member)
        .boardTitle(boardTitle)
        .boardComment(boardComment)
        .viewCount(viewCount)
        .likeCount(likeCount)
        .commentCount(commentCount)
        .boardType(boardType)
        .build();
  }

  public static BoardDto toDto(Board entity, Member member){
    return BoardDto.builder()
        .boardId(entity.getBoardId())
        .member(member)
        .boardTitle(entity.getBoardTitle())
        .boardComment(entity.getBoardComment())
        .viewCount(entity.getViewCount())
        .likeCount(entity.getLikeCount())
        .commentCount(entity.getCommentCount())
        .boardType(entity.getBoardType())
        .createdDate(entity.getCreatedDate())
        .modifiedDate(entity.getModifiedDate())
        .build();
  }

}
