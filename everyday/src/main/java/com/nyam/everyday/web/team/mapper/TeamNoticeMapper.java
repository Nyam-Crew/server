package com.nyam.everyday.web.team.mapper;

import com.nyam.everyday.module.team.entity.TeamNotice;
import com.nyam.everyday.web.team.dto.TeamNoticeDTO;
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

    @Mapping(source = "Team.TeamId", target = "TeamId")
    @Mapping(source = "member.memberId", target = "memberId")
    TeamNoticeDTO toDTO(TeamNotice entity);

    @Mapping(source = "TeamId", target = "Team.TeamId")
    @Mapping(source = "memberId", target = "member.memberId")
    TeamNotice toEntity(TeamNoticeDTO dto);
}