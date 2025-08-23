package com.nyam.everyday.web.mission.mapper;

import com.nyam.everyday.module.mission.entity.DailyMission;
import com.nyam.everyday.web.mission.dto.DailyMissionResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MissionWebMapper {

    @Mapping(target = "dailyMissionId", source = "dailyMissionId")
    @Mapping(target = "missionId",     source = "mission.missionId")
    @Mapping(target = "category",      source = "mission.category")
    @Mapping(target = "title",         source = "mission.title")
    @Mapping(target = "type",          source = "mission.type")
    @Mapping(target = "missionDate",   source = "missionDate")
    @Mapping(target = "completed",     source = "completed")      // boolean 필드 이름 주의
    @Mapping(target = "completedBy",   source = "completedBy")
    DailyMissionResponseDto toDailyMissionResponse(DailyMission entity);

    List<DailyMissionResponseDto> toDailyMissionResponse(List<DailyMission> entities);
}