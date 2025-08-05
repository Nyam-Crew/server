package com.nyam.everyday.web.meal.controller;

import com.nyam.everyday.web.meal.dto.MealLogRequestDto;
import com.nyam.everyday.web.meal.dto.MealLogResponseDto;
import com.nyam.everyday.module.meal.service.MealLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // 날짜별 기록 조회 (ex: /api/meal/log?mealType=1&date=2025-08-05)
    @GetMapping("/log")
    public List<MealLogResponseDto> getMealLogs(
            // @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam Long memberId,
            @RequestParam String mealType,
            @RequestParam String date
    ) {
        return mealLogService.getMealLogs(memberId, mealType, date);
    }

    // 음식 기록 추가 (ex: POST /api/meal/log)
    @PostMapping("/log")
    public void addMealLog(@RequestBody MealLogRequestDto requestDto) {
        mealLogService.addMealLog(requestDto);
    }

    // 수정, 삭제 등 다른 API도 필요한 만큼 추가 가능!
}
