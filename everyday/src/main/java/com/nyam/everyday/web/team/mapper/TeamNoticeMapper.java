package com.nyam.everyday.web.team.mapper;

import com.nyam.everyday.module.team.entity.TeamNotice;
import com.nyam.everyday.web.team.dto.TeamNoticeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 그룹 공지 Builder mapper
 *
 * @author : 이지은
 * @fileName : TeamNoticeMapper
 * @since : 25. 8. 4.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamNoticeMapper {

    @Mapping(source = "team.teamId", target = "teamId", ignore = true)
    @Mapping(source = "member.memberId", target = "memberId", ignore = true)
    TeamNoticeDto toDTO(TeamNotice entity);

    @Mapping(source = "teamId", target = "team.teamId", ignore = true)
    @Mapping(source = "memberId", target = "member.memberId", ignore = true)
    TeamNotice toEntity(TeamNoticeDto dto);
}