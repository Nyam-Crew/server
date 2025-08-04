package com.nyam.everyday.web.bookmark.dto;

import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.module.bookmark.entity.Bookmark;
import com.nyam.everyday.module.member.entity.Member;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookmarkDto {

  private Long bookmark_id;
  private Member member_id;
  private Board board_id;
  private LocalDateTime created_date;

  //dto->entity로 변환하는 메서드
  public Bookmark toEntity(Member member,Board board) {
    return Bookmark.builder()
        .bookmark_id(bookmark_id)
        .member_id(member)
        .board_id(board)
        .build();
  }


  public static BookmarkDto toDto(Bookmark entity) {
    return BookmarkDto.builder()
        .bookmark_id(entity.getBookmark_id())
        .member_id(entity.getMember_id())
        .board_id(entity.getBoard_id())
        .build();
  }
}
