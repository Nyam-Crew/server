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

  private Long bookmarkId;
  private Member memberId;
  private Board boardId;
  private LocalDateTime createdDate;

  //dto->entity로 변환하는 메서드
  public Bookmark toEntity(Member member,Board board) {
    return Bookmark.builder()
        .bookmarkId(bookmarkId)
        .member(member)
        .board(board)
        .build();
  }


  public static BookmarkDto toDto(Bookmark entity) {
    return BookmarkDto.builder()
        .bookmarkId(entity.getBookmarkId())
        .memberId(entity.getMember())
        .boardId(entity.getBoard())
        .build();
  }
}
