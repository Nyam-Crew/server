package com.nyam.everyday.web.groub.mapper;

import com.nyam.everyday.module.group.entity.GroupRankingHistory;
import com.nyam.everyday.web.groub.dto.GroupRankingHistoryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 그룹 랭킹 백업 Builder mapper
 *
 * @author : 이지은
 * @fileName : GroupRankingHistoryMapper
 * @since : 25. 8. 4.
 */
@Mapper(componentModel = "spring")
public interface GroupRankingHistoryMapper {

    @Mapping(source = "group.groupId", target = "groupId")
    @Mapping(source = "member.memberId", target = "memberId")
    GroupRankingHistoryDTO toDTO(GroupRankingHistory entity);

    @Mapping(source = "groupId", target = "group.groupId")
    @Mapping(source = "memberId", target = "member.memberId")
    GroupRankingHistory toEntity(GroupRankingHistoryDTO dto);
}