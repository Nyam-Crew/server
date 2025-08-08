package com.nyam.everyday.web.team.mapper;

import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.web.team.dto.TeamDto;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 그룹 crud 관련 mapper
 *
 * @author : 이지은
 * @fileName : TeamMapper
 * @since : 25. 8. 4.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = false))
public interface TeamMapper {

    TeamMapper INSTANCE = Mappers.getMapper(TeamMapper.class);

    // DTO → Entity
    @Mapping(target = "teamCreatedDate", ignore = true) // 자동 생성 필드는 무시
    Team toEntity(TeamDto dto);

    // Entity → DTO
    TeamDto toDto(Team entity);
}