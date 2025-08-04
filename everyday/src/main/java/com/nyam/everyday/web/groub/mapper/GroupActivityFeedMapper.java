package com.nyam.everyday.web.groub.mapper;

import com.nyam.everyday.module.group.entity.GroupActivityFeed;
import com.nyam.everyday.web.groub.dto.GroupActivityFeedDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 그룹 실시간 현황 Builder mapper
 *
 * @author : 이지은
 * @fileName : GroupActivityFeedMapper
 * @since : 25. 8. 4.
 */
@Mapper(componentModel = "spring")
public interface GroupActivityFeedMapper {

    @Mapping(source = "group.groupId", target = "groupId")
    @Mapping(source = "member.memberId", target = "memberId")
    GroupActivityFeedDTO toDTO(GroupActivityFeed entity);

    @Mapping(source = "groupId", target = "group.groupId")
    @Mapping(source = "memberId", target = "member.memberId")
    GroupActivityFeed toEntity(GroupActivityFeedDTO dto);
}