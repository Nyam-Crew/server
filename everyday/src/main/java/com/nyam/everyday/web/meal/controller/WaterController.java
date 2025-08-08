package com.nyam.everyday.web.meal.controller;

import com.nyam.everyday.module.summary.service.MemberDailySummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * WaterController
 *
 * @author : 장소희
 * @fileName : WaterController
 * @since : 25. 8. 7.
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meal")
public class WaterController {
    MemberDailySummaryService memberDailySummaryService;
}
