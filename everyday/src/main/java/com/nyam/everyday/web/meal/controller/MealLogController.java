package com.nyam.everyday.web.meal.controller;

import com.nyam.everyday.module.summary.service.MemberDailySummaryService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.meal.dto.MealLogRequestDto;
import com.nyam.everyday.web.meal.dto.MealLogResponseDto;
import com.nyam.everyday.module.meal.service.MealLogService;
import com.nyam.everyday.web.meal.dto.WaterRequestDto;
import com.nyam.everyday.web.meal.dto.WeightRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * MealLogController
 *
 * @author : 장소희
 * @fileName : MealLogController
 * @since : 25. 8. 5.
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meal")
public class MealLogController {

    private final MealLogService mealLogService;
    private final MemberDailySummaryService memberDailySummaryService;

    // feat: 날짜별 기록 조회 (/api/meal/log?mealType=LUNCH&date=2025-08-05&memberId=1)
    @GetMapping("/log")
    public List<MealLogResponseDto> getMealLogs(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String mealType,
            @RequestParam String date
    ) {
        // JWT 인증으로 memberId 추출 및 설정
        return mealLogService.getMealLogs(userDetails.getId(), mealType, date);
    }

    // feat: 음식 기록 추가 (POST /api/meal/log)
    @PostMapping("/log")
    public ResponseEntity<?> addMealLog(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MealLogRequestDto requestDto
    ) {
        requestDto.setMemberId(userDetails.getId());

        Long mealLogId = mealLogService.addMealLog(requestDto);
        return ResponseEntity.ok(Map.of(
                "result", "ok",
                "mealLogId", mealLogId
        ));
    }

    // feat: 음식 기록 일부 수정 (intakeAmount, intakeKcal)
    @PatchMapping("/log/{mealLogId}")
    public ResponseEntity<?> updateMealLog(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long mealLogId,
            @RequestBody Map<String, Object> updates
    ){
        Integer intakeAmount = (Integer) updates.get("intakeAmount");
        Double intakeKcal = updates.get("intakeKcal") instanceof Integer ?
                ((Integer) updates.get("intakeKcal")).doubleValue() :
                (Double) updates.get("intakeKcal");
        mealLogService.updateIntakeAmountAndKcal(userDetails.getId(), mealLogId, intakeAmount, intakeKcal);
        return ResponseEntity.ok(Map.of("result", "ok", "mealLogId", mealLogId));
    }

    // feat: 삭제 기능
    @DeleteMapping("/log/{mealLogId}")
    public ResponseEntity<Void> deleteMealLog(@PathVariable Long mealLogId) {
        mealLogService.deleteMealLog(mealLogId);
        return ResponseEntity.noContent().build(); // 204 No Content 리턴
    }

    // 물 섭취 기록 추가
    @PostMapping("/water")
    public ResponseEntity<?> addWater(@RequestBody WaterRequestDto waterRequestDto,
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        // JWT에서 memberId 추출
        Long memberId = userDetails.getId();

        // 서비스에 위임
        memberDailySummaryService.addOrUpdateWater(memberId, waterRequestDto.getAmount());

        return ResponseEntity.ok(Map.of("result", "ok"));
    }

    @PostMapping("/weight")
    public ResponseEntity<?> addWeight(
            @RequestBody WeightRequestDto weightRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();

        memberDailySummaryService.addOrUpdateWeight(memberId, weightRequestDto.getWeight());

        return ResponseEntity.ok(Map.of("result", "ok"));
    }
}
