package com.nyam.everyday.web.team.mapper;

import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.web.team.dto.TeamDto;
import com.nyam.everyday.web.team.dto.TeamMemberDTO;
import com.nyam.everyday.web.team.dto.TeamMemberStatusDto;
import org.mapstruct.*;

import java.util.List;

/**
 * 그룹 참여 현황 Builder mapper
 *
 * @author : 이지은
 * @fileName : TeamMemberStatusMapper
 * @since : 25. 8. 4.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamMemberStatusMapper {

    @Mapping(target = "teamId", source = "team.teamId")
    @Mapping(target = "memberId", source = "member.memberId")
    TeamMemberStatusDto toStatusDto(TeamMemberStatus entity);

    @Mapping(source = "member.memberId", target = "memberId")
    @Mapping(source = "member.nickname", target = "nickname")
    @Mapping(source = "member.memberImg", target = "memberImg")
    @Mapping(source = "teamRole", target = "teamRole")
    TeamMemberDTO totoMemberDto(TeamMemberStatus status);

    List<TeamMemberStatusDto> toStatusDtoList(List<TeamMemberStatus> list);
    List<TeamMemberDTO> toMemberDtoList(List<TeamMemberStatus> list);

    @Mapping(target = "team", source = "team")
    @Mapping(target = "member", source = "member")
    TeamMemberStatus toEntity(TeamMemberStatusDto dto, Team team, Member member);

    // DTO로 기존 Entity 수정 (선택사항)
    @Mappings({
            @Mapping(target = "team", ignore = true),
            @Mapping(target = "member", ignore = true),
            @Mapping(target = "createdDate", ignore = true),
            @Mapping(target = "modifiedDate", ignore = true)
    })
    TeamMemberStatus modify(TeamMemberStatusDto dto, @MappingTarget TeamMemberStatus entity);

}
