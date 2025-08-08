package com.nyam.everyday.web.team.mapper;

import com.nyam.everyday.module.team.entity.TeamNotice;
import com.nyam.everyday.web.team.dto.TeamNoticeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 그룹 공지 Builder mapper
 *
 * @author : 이지은
 * @fileName : TeamNoticeMapper
 * @since : 25. 8. 4.
 */
@Mapper(componentModel = "spring")
public interface TeamNoticeMapper {

    @Mapping(source = "team.teamId", target = "teamId")
    @Mapping(source = "member.memberId", target = "memberId")
    TeamNoticeDto toDTO(TeamNotice entity);

    @Mapping(source = "teamId", target = "team.teamId")
    @Mapping(source = "memberId", target = "member.memberId")
    TeamNotice toEntity(TeamNoticeDto dto);
}