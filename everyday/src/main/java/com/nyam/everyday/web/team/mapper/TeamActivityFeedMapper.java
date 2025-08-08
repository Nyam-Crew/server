package com.nyam.everyday.web.team.mapper;

import com.nyam.everyday.module.team.entity.TeamActivityFeed;
import com.nyam.everyday.web.team.dto.TeamActivityFeedDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 그룹 실시간 현황 Builder mapper
 *
 * @author : 이지은
 * @fileName : teamActivityFeedMapper
 * @since : 25. 8. 4.
 */
@Mapper(componentModel = "spring")
public interface TeamActivityFeedMapper {

    @Mapping(source = "team.teamId", target = "teamId")
    @Mapping(source = "member.memberId", target = "memberId")
    TeamActivityFeedDto toDTO(TeamActivityFeedDto dto);

    @Mapping(source = "teamId", target = "team.teamId")
    @Mapping(source = "memberId", target = "member.memberId")
    TeamActivityFeed toEntity(TeamActivityFeed entity);
}