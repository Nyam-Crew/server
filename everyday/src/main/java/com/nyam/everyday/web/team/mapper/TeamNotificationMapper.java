package com.nyam.everyday.web.team.mapper;

import com.nyam.everyday.module.team.entity.TeamNotification;
import com.nyam.everyday.web.team.dto.TeamNotificationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 그룹 알림 Builder mapper
 *
 * @author : 이지은
 * @fileName : TeamNotificationMapper
 * @since : 25. 8. 4.
 */
@Mapper(componentModel = "spring")
public interface TeamNotificationMapper {

    @Mapping(source = "team.teamId", target = "teamId")
    @Mapping(source = "member.memberId", target = "memberId")
    TeamNotificationDto toDTO(TeamNotification entity);

    @Mapping(source = "teamId", target = "team.teamId")
    @Mapping(source = "memberId", target = "member.memberId")
    TeamNotification toEntity(TeamNotificationDto dto);
}
