package com.nyam.everyday.web.boardlike.mapper;


import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.module.boardLike.entity.BoardLike;
import com.nyam.everyday.module.bookmark.entity.Bookmark;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.web.boardlike.dto.BoardLikeResponseDto;
import com.nyam.everyday.web.bookmark.dto.CreateBookmarkResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BoardLikeMapper {

  default BoardLike toEntity(Board board, Member member) {
    return BoardLike.builder()
        .board(board)
        .member(member)
        .build();
  }


}
