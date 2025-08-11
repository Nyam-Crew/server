package com.nyam.everyday.web.team.mapper;

import com.nyam.everyday.module.team.entity.TeamNotification;
import com.nyam.everyday.web.team.dto.TeamNotificationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 그룹 알림 Builder mapper
 *
 * @author : 이지은
 * @fileName : TeamNotificationMapper
 * @since : 25. 8. 4.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamNotificationMapper {

    @Mapping(source = "team.teamId", target = "teamId", ignore = true)
    @Mapping(source = "member.memberId", target = "memberId", ignore = true)
    TeamNotificationDto toDTO(TeamNotification entity);

    @Mapping(source = "teamId", target = "team.teamId", ignore = true)
    @Mapping(source = "memberId", target = "member.memberId", ignore = true)
    TeamNotification toEntity(TeamNotificationDto dto);
}
