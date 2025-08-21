package com.nyam.everyday.web.member.mapper;

import com.nyam.everyday.module.board.dto.BoardWithNicknameDto;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.web.member.dto.MemberRequestDto;
import com.nyam.everyday.web.member.dto.MemberResponseDto;
import com.nyam.everyday.web.member.dto.MyBoardsResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberMapper {
  MemberResponseDto toDto(Member member); // Entity → DTO
  Member toEntity(MemberRequestDto dto); // DTO → Entity

  @Mappings({
      @Mapping(target = "memberId", ignore = true), // 수정 시 ID는 유지
      @Mapping(target = "providerId", ignore = true),
      @Mapping(target = "memberStatus", ignore = true),
      @Mapping(target = "createdDate", ignore = true)
  })
  Member modify(MemberRequestDto dto, @MappingTarget Member member);

  MyBoardsResponseDto toMyBoardsResponseDto(BoardWithNicknameDto boardWithNicknameDto);

}
