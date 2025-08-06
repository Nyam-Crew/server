package com.nyam.everyday.web.team.mapper;

import com.nyam.everyday.module.team.entity.TeamGlobalRanking;
import com.nyam.everyday.web.team.dto.TeamGlobalRankingDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

/**
 * 그룹 간 경쟁 mapper
 *
 * @author : 이지은
 * @fileName : TeamGlobalRankingMapper
 * @since : 25. 8. 6.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamGlobalRankingMapper {

    TeamGlobalRankingDto toDto(TeamGlobalRanking entity);

    TeamGlobalRanking toEntity(TeamGlobalRankingDto dto);

    @Mapping(target = "team.teamId", ignore = true)
    @Mapping(target = "id", ignore = true)
    TeamGlobalRanking toEntityIgnoreIds(TeamGlobalRankingDto dto);

}
