package com.nyam.everyday.web.team.mapper;

import com.nyam.everyday.module.team.entity.TeamRankingHistory;
import com.nyam.everyday.web.team.dto.TeamRankingHistoryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 그룹 랭킹 백업 Builder mapper
 *
 * @author : 이지은
 * @fileName : TeamRankingHistoryMapper
 * @since : 25. 8. 4.
 */
@Mapper(componentModel = "spring")
public interface TeamRankingHistoryMapper {

    @Mapping(source = "team.teamId", target = "teamId")
    @Mapping(source = "member.memberId", target = "memberId")
    TeamRankingHistoryDto toDTO(TeamRankingHistory entity);

    @Mapping(source = "teamId", target = "team.teamId")
    @Mapping(source = "memberId", target = "member.memberId")
    TeamRankingHistory toEntity(TeamRankingHistoryDto dto);
}