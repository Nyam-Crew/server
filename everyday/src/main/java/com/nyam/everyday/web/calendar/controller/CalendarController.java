package com.nyam.everyday.web.calendar.controller;

import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.module.calendar.service.CalendarService;
import com.nyam.everyday.web.calendar.dto.MonthlyCalendarResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Calendar-Controller", description = "월간 캘린더 조회 API")
@SecurityRequirement(name = "bearerAuth")
public class CalendarController {

    private final CalendarService calendarService;

    /**
     * 월간 캘린더 조회
     * GET /api/calendar/month?year=2025&month=8
     * year, month가 없으면 오늘 기준 월로 반환
     */
    @Operation(
            summary = "월간 캘린더 조회",
            description = "요청한 연/월의 캘린더 그리드를 반환합니다. year/month 미지정 시 현재 월 기준으로 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MonthlyCalendarResponse.class))
    )
    @GetMapping("/api/calendar/month")
    public MonthlyCalendarResponse getMonthlyCalendar(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails,

            @Parameter(description = "연도(예: 2025)", example = "2025")
            @RequestParam(required = false) Integer year,

            @Parameter(description = "월(1~12, 예: 8)", example = "8")
            @RequestParam(required = false) Integer month
    ) {
        Long memberId = userDetails.getId();
        return calendarService.getMonthly(memberId, year, month);
    }
}