package com.nyam.everyday.web.groub.mapper;

import com.nyam.everyday.module.group.entity.GroupMemberStatus;
import com.nyam.everyday.web.groub.dto.GroupMemberStatusDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 그룹 참여 현황 Builder mapper
 *
 * @author : 이지은
 * @fileName : GroupMemberStatusMapper
 * @since : 25. 8. 4.
 */
@Mapper(componentModel = "spring")
public interface GroupMemberStatusMapper {

    @Mapping(source = "group.groupId", target = "groupId")
    @Mapping(source = "member.memberId", target = "memberId")
    GroupMemberStatusDTO toDTO(GroupMemberStatus entity);

    @Mapping(source = "groupId", target = "group.groupId")
    @Mapping(source = "memberId", target = "member.memberId")
    GroupMemberStatus toEntity(GroupMemberStatusDTO dto);
}
