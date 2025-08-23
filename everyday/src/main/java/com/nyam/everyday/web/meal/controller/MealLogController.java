package com.nyam.everyday.web.meal.controller;

import com.nyam.everyday.module.meal.service.MealInsightsService;
import com.nyam.everyday.module.meal.service.MealLogService;
import com.nyam.everyday.module.meal.type.MealType;
import com.nyam.everyday.module.summary.service.MemberDailySummaryService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.meal.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@Tag(name = "Meal-Log-Controller", description = "식사 기록 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meal")
@Validated
public class MealLogController {

    private final MealLogService mealLogService;
    private final MemberDailySummaryService memberDailySummaryService;
    private final MealInsightsService mealInsightsService;

    // ------------------------------------------------------------------------
    // [READ] 특정 날짜 + 식사타입별 식사 기록 조회
    // ------------------------------------------------------------------------

    @Operation(
            summary = "날짜별 기록 조회",
            description = "회원의 특정 날짜, 식사 타입별 식사 기록 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = MealLogResponseDto.class)))
            }
    )
    @GetMapping("/log") // NOTE: 운영 중 경로 호환을 위해 기존 /log 유지 (권장 경로는 /logs)
    public ResponseEntity<List<MealLogResponseDto>> getMealLogs(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "식사 타입", required = true)
            @RequestParam @NotNull MealType mealType,
            @Parameter(description = "조회 날짜 (yyyy-MM-dd)", example = "2025-08-05", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate date
    ) {
        // Controller는 파라미터 검증과 변환만, 실제 조회는 Service로 위임
        List<MealLogResponseDto> body = mealLogService.getMealLogs(user.getId(), mealType, date.toString());
        return ResponseEntity.ok(body);
    }

    // ------------------------------------------------------------------------
    // [CREATE] 음식 기록 추가
    // ------------------------------------------------------------------------

    @Operation(summary = "음식 기록 추가", description = "새로운 음식 섭취 기록을 추가합니다.")
    @PostMapping("/log")
    public ResponseEntity<Map<String, Object>> addMealLog(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody MealLogRequestDto requestDto
    ) {
        requestDto.setMemberId(user.getId());
        Long createdId = mealLogService.addMealLog(requestDto);

        // ✅ 200 OK 로 통일
        return ResponseEntity.ok(Map.of("result", "ok", "mealLogId", createdId));
    }

    // ------------------------------------------------------------------------
    // [PATCH] 음식 기록 일부 수정 (섭취량/칼로리/3대영양소)
    // ------------------------------------------------------------------------

    @Operation(
            summary = "음식 기록 일부 수정",
            description = "섭취량(intakeAmount), 칼로리(intakeKcal), 3대영양소(protein/carbohydrate/fat)를 부분 수정합니다."
    )
    @PatchMapping("/log/{mealLogId}")
    public ResponseEntity<Map<String, Object>> updateMealLog(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long mealLogId,
            @RequestBody Map<String, Object> updates
    ) {
        // ⚠️ Map 기반 입력은 필드 오타/타입 오류에 취약함 → 추후 DTO 전환 권장(예: MealLogPartialUpdateRequest)
        Integer intakeAmount = asInteger(updates.get("intakeAmount"));
        Double intakeKcal   = asDouble(updates.get("intakeKcal"));
        BigDecimal protein       = asBigDecimal(updates.get("protein"));
        BigDecimal carbohydrate  = asBigDecimal(updates.get("carbohydrate"));
        BigDecimal fat           = asBigDecimal(updates.get("fat"));

        // 날짜는 바디에서 받지 않음: Service가 해당 로그의 mealLogDate로 summary 갱신
        mealLogService.updateIntakeAmountAndKcal(
                user.getId(), mealLogId, intakeAmount, intakeKcal, protein, carbohydrate, fat
        );

        return ResponseEntity.ok(Map.of("result", "ok", "mealLogId", mealLogId));
    }

    // ------------------------------------------------------------------------
    // [DELETE] 음식 기록 삭제
    // ------------------------------------------------------------------------

    @Operation(
            summary = "음식 기록 삭제",
            description = "해당 식사 기록을 삭제하고, 일일 요약에서 영양소를 차감합니다."
    )
    @DeleteMapping("/log/{mealLogId}")
    public ResponseEntity<Void> deleteMealLog(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long mealLogId
    ) {
        // Service에서 mealLogDate 기준으로 summary 차감까지 처리
        mealLogService.deleteMealLog(user.getId(), mealLogId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // ------------------------------------------------------------------------
    // [UPSERT] 물 섭취/체중 기록
    // ------------------------------------------------------------------------

    @Operation(summary = "물 섭취 기록 추가", description = "하루 물 섭취량을 기록하거나 수정합니다.")
    @PostMapping("/water")
    public ResponseEntity<Map<String, String>> addWater(
            @Valid @RequestBody WaterRequestDto body,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        memberDailySummaryService.addOrUpdateWater(user.getId(), body.getAmount(), body.getDate());
        return ResponseEntity.ok(Map.of("result", "ok"));
    }

    @Operation(summary = "체중 기록 추가", description = "하루 체중을 기록하거나 수정합니다.")
    @PostMapping("/weight")
    public ResponseEntity<Map<String, String>> addWeight(
            @Valid @RequestBody WeightRequestDto body,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        memberDailySummaryService.addOrUpdateWeight(user.getId(), body.getWeight(), body.getDate());
        return ResponseEntity.ok(Map.of("result", "ok"));
    }

    // ------------------------------------------------------------------------
    // [READ] 하루 요약/인사이트
    // ------------------------------------------------------------------------

    @Operation(
            summary = "하루 요약 조회",
            description = "식사별 totalKcal/섭취한 음식/물/체중을 반환합니다."
    )
    @GetMapping("/day/log")
    public ResponseEntity<MealDaySummaryResponseDto> getDaySummary(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate date
    ) {
        // Service 시그니처가 Date라면 Service 오버로드(LocalDate) 추가 권장
        MealDaySummaryResponseDto body = mealLogService.getDaySummary(user.getId(), java.sql.Date.valueOf(date));
        return ResponseEntity.ok(body);
    }

    @Operation(
            summary = "하루 분석(인사이트) 조회",
            description = "지정일(없으면 오늘)의 건강지표와 일일 섭취 요약을 통합 반환합니다."
    )
    @GetMapping("/day/insights")
    public ResponseEntity<DayInsightsResponseDto> getDayInsights(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate target = (date != null) ? date : LocalDate.now();
        DayInsightsResponseDto body = mealInsightsService.getDayInsights(user.getId(), target);
        return ResponseEntity.ok(body);
    }

    // =========================================================================
    // 내부 유틸: 느슨한 입력(Map) → 안전한 타입 변환
    // =========================================================================

    /**
     * 객체를 정수(Integer)로 변환한다. null 또는 공백은 null로 처리.
     */
    private Integer asInteger(Object raw) {
        if (raw == null) return null;
        if (raw instanceof Integer i) return i;
        if (raw instanceof Number n) return n.intValue();
        String s = raw.toString().trim();
        return s.isEmpty() ? null : Integer.valueOf(s);
    }

    /**
     * 객체를 실수(Double)로 변환한다. null 또는 공백은 null로 처리.
     */
    private Double asDouble(Object raw) {
        if (raw == null) return null;
        if (raw instanceof Double d) return d;
        if (raw instanceof Number n) return n.doubleValue();
        String s = raw.toString().trim();
        return s.isEmpty() ? null : Double.valueOf(s);
    }

    /**
     * 객체를 BigDecimal로 변환한다. null/공백은 BigDecimal.ZERO로 처리.
     * <p>업데이트에서 0으로 덮어쓰는 케이스를 지원하기 위해 기본값을 ZERO로 설정.</p>
     */
    private BigDecimal asBigDecimal(Object raw) {
        if (raw == null) return BigDecimal.ZERO;
        if (raw instanceof BigDecimal b) return b;
        String s = raw.toString().trim();
        return s.isEmpty() ? BigDecimal.ZERO : new BigDecimal(s);
    }
}