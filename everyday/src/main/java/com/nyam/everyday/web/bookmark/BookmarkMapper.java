package com.nyam.everyday.web.bookmark;


import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.module.bookmark.entity.Bookmark;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.web.bookmark.dto.CreateBookmarkRequestDto;
import com.nyam.everyday.web.bookmark.dto.CreateBookmarkResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookmarkMapper {

  @Mapping(target = "bookmarkId",ignore = true)
  @Mapping(target = "board",source = "board")
  @Mapping(target = "member",source = "member")
  Bookmark toEntity(CreateBookmarkRequestDto dto, Board board, Member member);

  @Mapping(source = "bookmark.bookmarkId", target = "bookmarkId")
  @Mapping(source = "bookmark.board.boardId",target = "boardId")
  @Mapping(source = "bookmark.member.memberId",target = "memberId")
  CreateBookmarkResponseDto toResponseDto(Bookmark bookmark);


}
