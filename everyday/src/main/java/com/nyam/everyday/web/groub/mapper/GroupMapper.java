package com.nyam.everyday.web.groub.mapper;

import com.nyam.everyday.module.group.entity.Group;
import com.nyam.everyday.web.groub.dto.GroupDTO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 그룹 crud 관련 mapper
 *
 * @author : 이지은
 * @fileName : GroupMapper
 * @since : 25. 8. 4.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = false))
public interface GroupMapper {

    GroupMapper INSTANCE = Mappers.getMapper(GroupMapper.class);

    // DTO → Entity
    @Mapping(target = "groupCreatedAt", ignore = true) // 자동 생성 필드는 무시
    Group toEntity(GroupDTO dto);

    // Entity → DTO
    GroupDTO toDto(Group entity);
}