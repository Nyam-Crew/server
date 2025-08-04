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


  private Long board_id;
  private Member member_id;
  private String board_title;
  private String board_comment;
  private Long view_count;
  private Long like_count;
  private Long comment_count;
  private String board_type;
  private LocalDateTime created_date;
  private LocalDateTime modified_date;


  public Board toEntity(Board board, Member member){
    return Board.builder()
        .board_id(board_id)
        .member_id(member)
        .board_title(board_title)
        .board_comment(board_comment)
        .view_count(view_count)
        .like_count(like_count)
        .comment_count(comment_count)
        .board_type(board_type)
        .created_date(created_date)
        .modified_date(modified_date)
        .build();
  }

  public static BoardDto toDto(Board entity,Member member){
    return BoardDto.builder()
        .board_id(entity.getBoard_id())
        .member_id(member)
        .board_title(entity.getBoard_title())
        .board_comment(entity.getBoard_comment())
        .view_count(entity.getView_count())
        .like_count(entity.getLike_count())
        .comment_count(entity.getComment_count())
        .board_type(entity.getBoard_type())
        .created_date(entity.getCreated_date())
        .modified_date(entity.getModified_date())
        .build();
  }

}
