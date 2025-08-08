package com.nyam.everyday.web.team.mapper;

import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.module.team.enums.ParticipationStatus;
import com.nyam.everyday.module.team.enums.TeamRole;
import com.nyam.everyday.web.team.dto.TeamDetailDto;
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

    @Mappings({@Mapping(source = "owner.memberId", target = "ownerId")})
    TeamDto toDto(Team team);// Entity → DTO

    @Mapping(target = "owner", source = "owner")
    Team toEntity(TeamDto dto, Member owner);// DTO + owner → Entity

    // DTO로 기존 Entity 수정 (선택사항)
    @Mappings({
            @Mapping(target = "teamId", ignore = true),
            @Mapping(target = "owner", ignore = true),
            @Mapping(target = "createdDate", ignore = true),
            @Mapping(target = "modifiedDate", ignore = true)
    })
    Team modify(TeamDto dto, @MappingTarget Team team);

    @Mappings({
            @Mapping(source = "team.teamId", target = "teamId"),
            @Mapping(source = "team.teamTitle", target = "teamTitle"),
            @Mapping(source = "team.teamDescription", target = "teamDescription"),
            @Mapping(source = "team.teamImg", target = "teamImage"),
            @Mapping(source = "team.teamMaxMembers", target = "maxMembers"),
            @Mapping(source = "team.teamCurrentMembers", target = "currentMemberCount"),
            @Mapping(source = "team.createdDate", target = "createdDate", dateFormat = "yyyy-MM-dd"),
            @Mapping(source = "status", target = "status"),
            @Mapping(source = "teamRole", target = "teamRole")
    })
    TeamDetailDto toDetailDto(Team team, ParticipationStatus status, TeamRole teamRole);
}