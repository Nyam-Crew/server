package com.nyam.everyday.web.team.mapper;

import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.web.team.dto.TeamMemberStatusDTO;
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

    @Mapping(source = "Team.TeamId", target = "TeamId")
    @Mapping(source = "member.memberId", target = "memberId")
    TeamMemberStatusDTO toDTO(TeamMemberStatus entity);

    @Mapping(source = "TeamId", target = "Team.TeamId")
    @Mapping(source = "memberId", target = "member.memberId")
    TeamMemberStatus toEntity(TeamMemberStatusDTO dto);
}
