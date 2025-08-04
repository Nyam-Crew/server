package com.nyam.everyday.web.groub.mapper;

import com.nyam.everyday.module.group.entity.GroupNotice;
import com.nyam.everyday.web.groub.dto.GroupNoticeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 그룹 공지 Builder mapper
 *
 * @author : 이지은
 * @fileName : GroupNoticeMapper
 * @since : 25. 8. 4.
 */
@Mapper(componentModel = "spring")
public interface GroupNoticeMapper {

    @Mapping(source = "group.groupId", target = "groupId")
    @Mapping(source = "member.memberId", target = "memberId")
    GroupNoticeDTO toDTO(GroupNotice entity);

    @Mapping(source = "groupId", target = "group.groupId")
    @Mapping(source = "memberId", target = "member.memberId")
    GroupNotice toEntity(GroupNoticeDTO dto);
}