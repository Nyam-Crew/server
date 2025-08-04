package com.nyam.everyday.web.groub.mapper;

import com.nyam.everyday.module.group.entity.GroupNotification;
import com.nyam.everyday.web.groub.dto.GroupNotificationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 그룹 알림 Builder mapper
 *
 * @author : 이지은
 * @fileName : GroupNotificationMapper
 * @since : 25. 8. 4.
 */
@Mapper(componentModel = "spring")
public interface GroupNotificationMapper {

    @Mapping(source = "group.groupId", target = "groupId")
    @Mapping(source = "member.memberId", target = "memberId")
    GroupNotificationDTO toDTO(GroupNotification entity);

    @Mapping(source = "groupId", target = "group.groupId")
    @Mapping(source = "memberId", target = "member.memberId")
    GroupNotification toEntity(GroupNotificationDTO dto);
}
