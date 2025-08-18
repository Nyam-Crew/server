package com.nyam.everyday.web.badge.mapper;

import com.nyam.everyday.module.badge.entity.Badge;
import com.nyam.everyday.web.badge.dto.BadgeResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BadgeMapper {

  BadgeResponseDto toDto(Badge entity);

  Badge toEntity(BadgeResponseDto dto);

}
