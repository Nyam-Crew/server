package com.nyam.everyday.web.team.mapper;

import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.web.team.dto.TeamMemberStatusDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 그룹 참여 현황 Builder mapper
 *
 * @author : 이지은
 * @fileName : TeamMemberStatusMapper
 * @since : 25. 8. 4.
 */
@Mapper(componentModel = "spring")
public interface TeamMemberStatusMapper {

    @Mapping(source = "team.teamId", target = "teamId")
    @Mapping(source = "member.memberId", target = "memberId")
    TeamMemberStatusDto toDTO(TeamMemberStatus entity);

    @Mapping(source = "teamId", target = "team.teamId")
    @Mapping(source = "memberId", target = "member.memberId")
    TeamMemberStatus toEntity(TeamMemberStatusDto dto);
}
