package com.nyam.everyday.web.team.mapper;

import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.web.team.dto.TeamDto;
import org.mapstruct.*;

/**
 * 그룹 crud 관련 mapper
 *
 * @author : 이지은
 * @fileName : TeamMapper
 * @since : 25. 8. 4.
 */

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamMapper {

    TeamDto toDto(Team team);// Entity → DTO
    Team toEntity(TeamDto dto, Member owner);// DTO + owner → Entity

    // DTO로 기존 Entity 수정 (선택사항)
    @Mappings({
            @Mapping(target = "teamId", ignore = true),
            @Mapping(target = "owner", ignore = true),
            @Mapping(target = "createdDate", ignore = true),
            @Mapping(target = "modifiedDate", ignore = true)
    })
    Team modify(TeamDto dto, @MappingTarget Team team);
}