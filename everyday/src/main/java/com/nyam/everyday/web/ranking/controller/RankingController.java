package com.nyam.everyday.web.ranking.controller;

import com.nyam.everyday.module.ranking.service.RankingService;
import com.nyam.everyday.web.ranking.dto.RankingDto;
import com.nyam.everyday.web.ranking.dto.TeamRankingDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Ranking-Controller", description = "실시간 랭킹 API")
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

  private final RankingService rankingService;

  @Operation(summary = "상위 N명 유저 조회", description = "전체 멤버 중 상위 N명의 랭킹을 조회합니다.")
  @GetMapping("/users/top")
  public ResponseEntity<List<RankingDto>> getTopRankers(
      @Parameter(description = "조회할 유저 수", example = "10") @RequestParam(defaultValue = "10") int limit,
      @Parameter(description = "조회할 연도 (예: 2025)", example = "2025") @RequestParam(required = false) Integer year,
      @Parameter(description = "조회할 월 (1-12)", example = "8") @RequestParam(required = false) Integer month) {
    return ResponseEntity.ok(rankingService.getTopRankers(limit, year, month));
  }

  @Operation(summary = "특정 유저 랭킹 조회", description = "특정 유저의 개인 랭킹과 점수를 조회합니다.")
  @GetMapping("/users/{memberId}")
  public ResponseEntity<RankingDto> getUserRank(
      @Parameter(description = "회원 ID", example = "310") @PathVariable Long memberId,
      @Parameter(description = "조회할 연도 (예: 2025)", example = "2025") @RequestParam(required = false) Integer year,
      @Parameter(description = "조회할 월 (1-12)", example = "8") @RequestParam(required = false) Integer month) {
    return ResponseEntity.ok(rankingService.getUserRank(memberId, year, month));
  }

  @Operation(summary = "팀간 랭킹 조회", description = "전체 팀들의 랭킹을 조회합니다.")
  @GetMapping("/teams/top")
  public ResponseEntity<List<TeamRankingDto>> getInterTeamRanking(
      @Parameter(description = "조회할 팀 수", example = "10") @RequestParam(defaultValue = "10") int limit,
      @Parameter(description = "조회할 연도 (예: 2025)", example = "2025") @RequestParam(required = false) Integer year,
      @Parameter(description = "조회할 월 (1-12)", example = "8") @RequestParam(required = false) Integer month) {
    return ResponseEntity.ok(rankingService.getInterTeamRanking(limit, year, month));
  }

  @Operation(summary = "팀 내부 멤버 랭킹 조회", description = "특정 팀에 소속된 멤버들의 랭킹을 조회합니다.")
  @GetMapping("/teams/{teamId}/members")
  public ResponseEntity<List<RankingDto>> getIntraTeamRanking(
      @Parameter(description = "팀 ID", example = "1") @PathVariable Long teamId,
      @Parameter(description = "조회할 유저 수", example = "10") @RequestParam(defaultValue = "10") int limit,
      @Parameter(description = "조회할 연도 (예: 2025)", example = "2025") @RequestParam(required = false) Integer year,
      @Parameter(description = "조회할 월 (1-12)", example = "8") @RequestParam(required = false) Integer month) {
    return ResponseEntity.ok(rankingService.getIntraTeamRanking(teamId, limit, year, month));
  }

  @DeleteMapping("/users")
  @Operation(summary = "개인 랭킹 초기화", description = "지정된 년/월의 전체 개인 랭킹을 삭제합니다.")
  public ResponseEntity<Void> clearUserRanking(
      @Parameter(description = "초기화할 연도", required = true) @RequestParam Integer year,
      @Parameter(description = "초기화할 월", required = true) @RequestParam Integer month) {
    rankingService.clearUserRanking(year, month);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/teams")
  @Operation(summary = "팀간 랭킹 초기화", description = "지정된 년/월의 전체 팀간 랭킹을 삭제합니다.")
  public ResponseEntity<Void> clearInterTeamRanking(
      @Parameter(description = "초기화할 연도", required = true) @RequestParam Integer year,
      @Parameter(description = "초기화할 월", required = true) @RequestParam Integer month) {
    rankingService.clearInterTeamRanking(year, month);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/teams/{teamId}")
  @Operation(summary = "팀 내부 랭킹 초기화", description = "지정된 년/월의 특정 팀 내부 랭킹을 삭제합니다.")
  public ResponseEntity<Void> clearIntraTeamRanking(
      @Parameter(description = "초기화할 팀 ID") @PathVariable Long teamId,
      @Parameter(description = "초기화할 연도", required = true) @RequestParam Integer year,
      @Parameter(description = "초기화할 월", required = true) @RequestParam Integer month) {
    rankingService.clearIntraTeamRanking(teamId, year, month);
    return ResponseEntity.ok().build();
  }
}
