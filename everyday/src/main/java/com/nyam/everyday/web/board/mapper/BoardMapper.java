package com.nyam.everyday.web.board.mapper;


import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.web.board.dto.BoardPageDto;
import com.nyam.everyday.web.board.dto.CreateBoardRequestDto;
import com.nyam.everyday.web.board.dto.BoardResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BoardMapper {


  //Entity->DTO
  @Mapping(target = "nickname",source = "member.nickname")//엔티티가 작성자 닉네임을 DTO로 맵핑
  BoardResponseDto toDto(Board entity);


  //DTO->Entity
  Board toResEntity(BoardResponseDto dto);


  //게시글 작성
  @Mapping(target = "member", source = "member")
  @Mapping(target = "viewCount", constant = "0L")
  @Mapping(target = "likeCount", constant = "0L")
  @Mapping(target = "commentCount", constant = "0L")
  Board toEntity(CreateBoardRequestDto dto, Member member);

  @Mappings({
      @Mapping(target = "boardId", ignore = true),
      @Mapping(target = "member",ignore = true), // 작성자는 변경하지 않음
      @Mapping(target = "viewCount", ignore = true),
      @Mapping(target = "likeCount", ignore = true),
      @Mapping(target = "commentCount", ignore = true),
      @Mapping(target = "createdDate", ignore = true),
      @Mapping(target = "modifiedDate",ignore = true)

  })
  void modify(@MappingTarget Board entity, BoardResponseDto dto);

  @Mapping(source = "member.nickname", target = "nickname")
  BoardPageDto toPageDto(Board entity);


}
