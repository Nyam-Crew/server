package com.nyam.everyday.web.team.mapper;

import com.nyam.everyday.module.team.entity.TeamRankingHistory;
import com.nyam.everyday.web.team.dto.TeamRankingHistoryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 그룹 랭킹 백업 Builder mapper
 *
 * @author : 이지은
 * @fileName : TeamRankingHistoryMapper
 * @since : 25. 8. 4.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamRankingHistoryMapper {

    @Mapping(source = "team.teamId", target = "teamId", ignore = true)
    @Mapping(source = "member.memberId", target = "memberId", ignore = true)
    TeamRankingHistoryDto toDTO(TeamRankingHistory entity);

    @Mapping(source = "teamId", target = "team.teamId", ignore = true)
    @Mapping(source = "memberId", target = "member.memberId", ignore = true)
    TeamRankingHistory toEntity(TeamRankingHistoryDto dto);
}