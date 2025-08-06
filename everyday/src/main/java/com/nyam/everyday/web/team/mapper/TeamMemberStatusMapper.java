package com.nyam.everyday.web.team.mapper;

import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.web.team.dto.TeamMemberStatusDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 그룹 참여 현황 Builder mapper
 *
 * @author : 이지은
 * @fileName : TeamMemberStatusMapper
 * @since : 25. 8. 4.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamMemberStatusMapper {

    @Mapping(source = "team.teamId", target = "teamId", ignore = true)
    @Mapping(source = "member.memberId", target = "memberId", ignore = true)
    TeamMemberStatusDto toDTO(TeamMemberStatus entity);

    @Mapping(source = "teamId", target = "team.teamId", ignore = true)
    @Mapping(source = "memberId", target = "member.memberId", ignore = true)
    TeamMemberStatus toEntity(TeamMemberStatusDto dto);
}
