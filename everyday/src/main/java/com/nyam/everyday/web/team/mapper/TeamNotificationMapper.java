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

    // ✅ Entity -> DTO: 연관관계 id, 생성일 매핑
    @Mapping(source = "team.teamId",    target = "teamId")
    @Mapping(source = "member.memberId", target = "memberId")
    @Mapping(source = "createdDate",       target = "teamAlarmCreatedDate")
    TeamNotificationDto toDto(TeamNotification entity);

    // ✅ DTO -> Entity: 연관관계는 서비스에서 set, Mapper에선 무시
    @Mapping(target = "teamAlarmId", ignore = true)
    @Mapping(target = "team",        ignore = true)
    @Mapping(target = "member",      ignore = true)
    TeamNotification toEntity(TeamNotificationDto dto);
}
