package com.nyam.everyday.web.member.mapper;

import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.web.member.dto.MemberDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface MemberMapper {
  MemberDto toDto(Member member); // Entity → DTO
  Member toEntity(MemberDto dto); // DTO → Entity

  @Mappings({
      @Mapping(target = "id", ignore = true // 수정 시 ID는 유지
      )}
  )
  Member modify(MemberDto dto, @MappingTarget Member member);

}
