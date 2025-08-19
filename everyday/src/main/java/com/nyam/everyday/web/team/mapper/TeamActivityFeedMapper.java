package com.nyam.everyday.web.team.mapper;

import com.nyam.everyday.module.team.entity.TeamActivityFeed;
import com.nyam.everyday.web.team.dto.TeamActivityFeedItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 그룹 실시간 현황 Builder mapper
 *
 * @author : 이지은
 * @fileName : TeamActivityFeedMapper
 * @since : 25. 8. 4.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamActivityFeedMapper {

    @Mapping(source = "team.teamId", target = "teamId")
    @Mapping(source = "member.memberId", target = "memberId")
    TeamActivityFeedItem toFeedDTO(TeamActivityFeed entity);

    @Mapping(source = "teamId", target = "team.teamId", ignore = true)
    @Mapping(source = "memberId", target = "member.memberId", ignore = true)
    TeamActivityFeed toEntity(TeamActivityFeedItem dto);
}