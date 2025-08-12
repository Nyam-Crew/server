package com.nyam.everyday.web.badge.mapper;

import com.nyam.everyday.module.badge.entity.Badge;
import com.nyam.everyday.web.badge.dto.BadgeDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BadgeMapper {

  BadgeDto toDto(Badge entity);

  Badge toEntity(BadgeDto dto);

}
