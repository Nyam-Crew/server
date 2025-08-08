package com.nyam.everyday.web.boardComment.mapper;


import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.module.boardComment.entity.BoardComment;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.web.boardComment.dto.CreateCommentRequestDto;
import com.nyam.everyday.web.boardComment.dto.CreateCommentResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BoardCommentMapper {

  // board, member는 서비스 단에서 조회 후 주입
  @Mappings({
      @Mapping(target = "board",source = "board"),
      @Mapping(target = "member", source = "member"),
      @Mapping(target = "createdDate",expression = "java(java.time.LocalDateTime.now())")

  })
  BoardComment toEntity(CreateCommentRequestDto dto, Board board, Member member);

  //entity에 저장된 속성을 CreateCommentResponseDto에 대입시킴
  @Mappings({
      @Mapping(source = "boardComment.board.boardId", target = "boardId"),
      @Mapping(source = "boardComment.member.memberId", target = "memberId"),
      @Mapping(source = "boardComment.commentId", target = "commentId"),
      @Mapping(source = "boardComment.createdDate", target = "createdDate")
  })
  CreateCommentResponseDto toResponseDto(BoardComment boardComment);

}
