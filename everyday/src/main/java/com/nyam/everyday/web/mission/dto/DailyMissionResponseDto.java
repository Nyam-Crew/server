package com.nyam.everyday.web.mission.dto;

import com.nyam.everyday.module.mission.entity.CompletedBy;
import com.nyam.everyday.module.mission.entity.MissionCategory;
import com.nyam.everyday.module.mission.entity.MissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

/*
 * 하루 미션 응답 DTO
 *
 * 설계 의도
 * - 특정 날짜의 미션 상세 정보를 클라이언트에 반환
 * - 미션 카테고리, 타입, 완료 여부, 완료 주체까지 포함
 */
@Value
@Builder
@Schema(description = "하루 미션 응답 DTO")
public class DailyMissionResponseDto {

    @Schema(description = "일일 미션 PK")
    Long dailyMissionId;

    @Schema(description = "미션 PK")
    Long missionId;

    @Schema(description = "미션 카테고리 (예: 운동, 식단 등)")
    MissionCategory category;

    @Schema(description = "미션 제목")
    String title;

    @Schema(description = "미션 타입 (예: 개별, 그룹)")
    MissionType type;

    @Schema(description = "미션 날짜")
    LocalDate missionDate;

    @Schema(description = "완료 여부")
    boolean completed;

    @Schema(description = "누가 완료했는지 (사용자/시스템)")
    CompletedBy completedBy;
}