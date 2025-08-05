package com.nyam.everyday.web.groub.mapper;

import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.web.groub.dto.TeamDTO;
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
    @Mapping(target = "TeamCreatedAt", ignore = true) // 자동 생성 필드는 무시
    Team toEntity(TeamDTO dto);

    // Entity → DTO
    TeamDTO toDto(Team entity);
}