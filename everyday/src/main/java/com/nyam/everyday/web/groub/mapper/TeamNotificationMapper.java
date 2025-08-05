package com.nyam.everyday.web.groub.mapper;

import com.nyam.everyday.module.team.entity.TeamNotification;
import com.nyam.everyday.web.groub.dto.TeamNotificationDTO;
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

    @Mapping(source = "Team.TeamId", target = "TeamId")
    @Mapping(source = "member.memberId", target = "memberId")
    TeamNotificationDTO toDTO(TeamNotification entity);

    @Mapping(source = "TeamId", target = "Team.TeamId")
    @Mapping(source = "memberId", target = "member.memberId")
    TeamNotification toEntity(TeamNotificationDTO dto);
}
