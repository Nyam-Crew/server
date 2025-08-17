package com.nyam.everyday.web.mission.controller;

import com.nyam.everyday.module.mission.entity.DailyMission;
import com.nyam.everyday.module.mission.service.MissionAssignmentService;
import com.nyam.everyday.module.mission.service.MissionProgressService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.mission.dto.DailyMissionResponse;
import com.nyam.everyday.web.mission.mapper.MissionWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.nyam.everyday.web.mission.dto.MissionCompleteRequest;
import org.springframework.http.ResponseEntity;
import java.util.List;

/**
 * MissionController
 * - 오늘 미션 조회(온디맨드 보정 포함)
 * - (참고) 완료 토글/캘린더 등 다른 API는 필요시 아래에 추가
 */
@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionAssignmentService missionAssignmentService;
    private final MissionProgressService missionProgressService;
    private final MissionWebMapper mapper;

    /**
     * 오늘 미션 조회 (온디맨드 보정)
     * - 오늘 데이터가 없거나 5개 미만이면 즉시 채워서 반환
     * - JWT로부터 memberId 추출 (쿼리 파라미터 불필요)
     */
    @GetMapping("/today")
    public List<DailyMissionResponse> getToday(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();
        List<DailyMission> list = missionAssignmentService.getOrAssignTodayMissions(memberId);
        return mapper.toDailyMissionResponse(list);
    }

    // ✅ 미션 완료/해제 (체크할 때마다 도장 발급/갱신)
    @PostMapping("/{dailyMissionId}/complete")
    public ResponseEntity<Void> completeMission(@PathVariable Long dailyMissionId,
                                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                                @RequestBody MissionCompleteRequest request) {
        missionProgressService.toggleManualCompletion(dailyMissionId, userDetails.getId(), request.isComplete());
        return ResponseEntity.ok().build();
    }

    /* =========================
       (선택) 완료 토글 예시
       - 필요 시 주석 해제/구현
       ========================= */
    // @PostMapping("/{dailyMissionId}/complete")
    // public DailyMissionResponse toggleComplete(@AuthenticationPrincipal CustomUser user,
    //                                            @PathVariable Long dailyMissionId,
    //                                            @RequestParam("complete") boolean complete) {
    //     Long memberId = user.getMemberId();
    //     DailyMission dm = missionProgressService.toggleManualComplete(memberId, dailyMissionId, complete);
    //     return mapper.toDailyMissionItem(dm);
    // }

    /* =========================
       (선택) 달력 조회 예시
       - 필요 시 주석 해제/구현
       ========================= */
    // @GetMapping("/calendar")
    // public StampCalendarResponse getCalendar(@AuthenticationPrincipal CustomUser user,
    //                                          @RequestParam String month) { // e.g. "2025-08"
    //     Long memberId = user.getMemberId();
    //     return stampService.getCalendar(memberId, month);
    // }
}