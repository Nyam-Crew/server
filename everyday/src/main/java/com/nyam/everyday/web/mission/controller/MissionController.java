package com.nyam.everyday.web.mission.controller;

import com.nyam.everyday.module.mission.entity.DailyMission;
import com.nyam.everyday.module.mission.service.MissionAssignmentService;
import com.nyam.everyday.module.mission.service.MissionProgressService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.mission.dto.DailyMissionResponse;
import com.nyam.everyday.web.mission.dto.MissionCompleteRequest;
import com.nyam.everyday.web.mission.mapper.MissionWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
@Tag(name = "Missions-Controller", description = "일일 미션 조회/완료 API")
@SecurityRequirement(name = "bearerAuth")
public class MissionController {

    private final MissionAssignmentService missionAssignmentService;
    private final MissionProgressService missionProgressService;
    private final MissionWebMapper mapper;

    @Operation(
            summary = "오늘 미션 조회",
            description = "오늘 데이터가 없거나 5개 미만이면 즉시 채워서 반환합니다. (온디맨드 보정)"
    )
    @ApiResponse(responseCode = "200", description = "성공",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DailyMissionResponse.class)))
    @GetMapping("/today")
    public List<DailyMissionResponse> getToday(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getId();
        List<DailyMission> list = missionAssignmentService.getOrAssignTodayMissions(memberId);
        return mapper.toDailyMissionResponse(list);
    }

    @Operation(
            summary = "미션 완료/해제",
            description = "체크/해제 시 스탬프(도장)를 자동 업서트합니다."
    )
    @ApiResponse(responseCode = "200", description = "업데이트 성공")
    @PostMapping("/{dailyMissionId}/complete")
    public ResponseEntity<Void> completeMission(
            @Parameter(name = "dailyMissionId", description = "일일 미션 PK", in = ParameterIn.PATH, required = true, example = "123")
            @PathVariable Long dailyMissionId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "완료 여부 본문",
                    required = true,
                    content = @Content(schema = @Schema(implementation = MissionCompleteRequest.class))
            )
            @RequestBody MissionCompleteRequest request
    ) {
        missionProgressService.toggleManualCompletion(dailyMissionId, userDetails.getId(), request.isComplete());
        return ResponseEntity.ok().build();
    }
}