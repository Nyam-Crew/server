package com.nyam.everyday.web.mission.mapper;

import com.nyam.everyday.module.mission.entity.DailyMission;
import com.nyam.everyday.web.mission.dto.DailyMissionResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/*
 * MissionWebMapper
 *
 * 설계 의도
 * - DailyMission 엔티티 → DailyMissionResponseDto 변환 담당
 * - MapStruct를 활용하여 반복적인 매핑 로직 제거
 * - 단일 엔티티와 리스트 모두 매핑 지원
 */
@Mapper(componentModel = "spring")
public interface MissionWebMapper {

    @Mapping(target = "dailyMissionId", source = "dailyMissionId")
    @Mapping(target = "missionId",     source = "mission.missionId")
    @Mapping(target = "category",      source = "mission.category")
    @Mapping(target = "title",         source = "mission.title")
    @Mapping(target = "type",          source = "mission.type")
    @Mapping(target = "missionDate",   source = "missionDate")
    @Mapping(target = "completed",     source = "completed")    // boolean 필드 주의
    @Mapping(target = "completedBy",   source = "completedBy")
    DailyMissionResponseDto toDailyMissionResponse(DailyMission entity);

    List<DailyMissionResponseDto> toDailyMissionResponse(List<DailyMission> entities);
}