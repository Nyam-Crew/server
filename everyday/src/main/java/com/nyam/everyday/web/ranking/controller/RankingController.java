package com.nyam.everyday.web.ranking.controller;

import com.nyam.everyday.web.ranking.dto.RankingDto;
import com.nyam.everyday.module.scorelog.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Ranking", description = "실시간 랭캉 API")
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @Operation(summary = "상위 N명 유저 조회", description = "랭킹 내 상위 N명의 유저를 조회합니다.")
    @GetMapping("/top")
    public ResponseEntity<List<RankingDto>> getTopRankers(
        @Parameter(description = "조회할 유저 수", example = "10") @RequestParam(defaultValue = "10") int limit,
        @Parameter(description = "조회할 연도 (예: 2025)", example = "2025") @RequestParam(required = false) Integer year,
        @Parameter(description = "조회할 월 (1-12)", example = "8") @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(rankingService.getTopRankers(limit, year, month));
    }

    @Operation(summary = "유저 랭킹 조회", description = "특정 유저의 랭킹과 점수를 조회합니다.")
    @GetMapping("/user/{memberId}")
    public ResponseEntity<RankingDto> getUserRank(
        @Parameter(description = "회원 ID", example = "310") @PathVariable Long memberId,
        @Parameter(description = "조회할 연도 (예: 2025)", example = "2025") @RequestParam(required = false) Integer year,
        @Parameter(description = "조회할 월 (1-12)", example = "8") @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(rankingService.getUserRank(memberId, year, month));
    }
}
