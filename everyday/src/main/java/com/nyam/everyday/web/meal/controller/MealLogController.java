package com.nyam.everyday.web.meal.controller;

import com.nyam.everyday.module.summary.service.MemberDailySummaryService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.meal.dto.*;
import com.nyam.everyday.module.meal.service.MealLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "Meal-Log-Controller", description = "식사 기록 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meal")
public class MealLogController {

    private final MealLogService mealLogService;
    private final MemberDailySummaryService memberDailySummaryService;

    // feat: 날짜별 기록 조회 (/api/meal/log?mealType=LUNCH&date=2025-08-05)
    @Operation(summary = "날짜별 기록 조회", description = "회원의 특정 날짜, 식사 타입별 식사 기록 목록을 조회합니다.")
    @GetMapping("/log")
    public List<MealLogResponseDto> getMealLogs(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String mealType,
            @RequestParam String date
    ) {
        return mealLogService.getMealLogs(userDetails.getId(), mealType, date);
    }

    // feat: 음식 기록 추가 (POST /api/meal/log)
    @Operation(summary = "음식 기록 추가", description = "새로운 음식 섭취 기록을 추가합니다.")
    @PostMapping("/log")
    public ResponseEntity<?> addMealLog(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MealLogRequestDto requestDto
    ) {
        requestDto.setMemberId(userDetails.getId()); // JWT에서 memberId 주입
        Long mealLogId = mealLogService.addMealLog(requestDto);
        return ResponseEntity.ok(Map.of(
                "result", "ok",
                "mealLogId", mealLogId
        ));
    }

    // feat: 음식 기록 일부 수정 (intakeAmount, intakeKcal, 탄/단/지)
    @Operation(summary = "음식 기록 일부 수정", description = "섭취량/칼로리/3대영양소를 부분 수정합니다.")
    @PatchMapping("/log/{mealLogId}")
    public ResponseEntity<?> updateMealLog(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long mealLogId,
            @RequestBody Map<String, Object> updates
    ){
        Integer intakeAmount = (Integer) updates.get("intakeAmount");
        Double intakeKcal = updates.get("intakeKcal") instanceof Integer
                ? ((Integer) updates.get("intakeKcal")).doubleValue()
                : (Double) updates.get("intakeKcal");

        BigDecimal protein = updates.get("protein") != null
                ? new BigDecimal(updates.get("protein").toString())
                : BigDecimal.ZERO;

        BigDecimal carbohydrate = updates.get("carbohydrate") != null
                ? new BigDecimal(updates.get("carbohydrate").toString())
                : BigDecimal.ZERO;

        BigDecimal fat = updates.get("fat") != null
                ? new BigDecimal(updates.get("fat").toString())
                : BigDecimal.ZERO;

        // ✅ 날짜는 바디에서 받지 않음. 서비스가 해당 로그의 mealLogDate로 summary 갱신
        mealLogService.updateIntakeAmountAndKcal(
                userDetails.getId(),
                mealLogId,
                intakeAmount,
                intakeKcal,
                protein,
                carbohydrate,
                fat
        );

        return ResponseEntity.ok(Map.of("result", "ok", "mealLogId", mealLogId));
    }

    // feat: 삭제 기능
    @Operation(summary = "음식 기록 삭제", description = "해당 식사 기록을 삭제하고, 일일 요약에서 영양소를 차감합니다.")
    @DeleteMapping("/log/{mealLogId}")
    public ResponseEntity<Void> deleteMealLog(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @PathVariable Long mealLogId) {
        // ✅ 서비스에서 해당 로그의 mealLogDate 기준으로 summary 차감
        mealLogService.deleteMealLog(userDetails.getId(), mealLogId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // 물 섭취 기록 추가 (현재는 오늘 기준. 선택일 기능이 필요하면 DTO/Service에 LocalDate date 추가로 확장)
    @Operation(summary = "물 섭취 기록 추가", description = "하루 물 섭취량을 기록하거나 수정합니다.")
    @PostMapping("/water")
    public ResponseEntity<?> addWater(@RequestBody WaterRequestDto waterRequestDto,
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();
        memberDailySummaryService.addOrUpdateWater(memberId, waterRequestDto.getAmount(), waterRequestDto.getDate());
        return ResponseEntity.ok(Map.of("result", "ok"));
    }

    @Operation(summary = "체중 기록 추가", description = "하루 체중을 기록하거나 수정합니다.")
    @PostMapping("/weight")
    public ResponseEntity<?> addWeight(
            @RequestBody WeightRequestDto weightRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();
        memberDailySummaryService.addOrUpdateWeight(memberId, weightRequestDto.getWeight(),  weightRequestDto.getDate());
        return ResponseEntity.ok(Map.of("result", "ok"));
    }

    @Operation(summary = "하루 요약 조회", description = "한 날짜의 식사별 목록(라이트) + 물/체중 + 총칼로리를 반환합니다.")
    @GetMapping("/day")
    public ResponseEntity<MealDayLiteResponse> getDay(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(mealLogService.getDay(user.getId(), date));
    }
}