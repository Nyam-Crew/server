package com.nyam.everyday.web.ranking.controller;

import com.nyam.everyday.module.ranking.service.RankingService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.ranking.dto.RankingDto;
import com.nyam.everyday.web.ranking.dto.TeamRankingDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Ranking-Controller", description = "실시간 랭킹 API")
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

  private final RankingService rankingService;

  @Operation(summary = "상위 N명 멤버 조회 (월간)", description = "전체 멤버 중 상위 N명의 월간 랭킹을 조회합니다.")
  @GetMapping("/members/top")
  public ResponseEntity<List<RankingDto>> getTopRankers(
      @Parameter(description = "조회할 멤버 수", example = "10") @RequestParam(defaultValue = "10") int limit,
      @Parameter(description = "조회할 연도 (예: 2025)", example = "2025") @RequestParam(required = false) Integer year,
      @Parameter(description = "조회할 월 (1-12)", example = "8") @RequestParam(required = false) Integer month) {
    return ResponseEntity.ok(rankingService.getTopRankers(limit, year, month));
  }

  @Operation(summary = "특정 멤버 랭킹 조회 (월간)", description = "특정 멤버의 월간 개인 랭킹과 점수를 조회합니다.")
  @GetMapping("/members/{memberId}")
  public ResponseEntity<RankingDto> getMemberRank(
      @Parameter(description = "멤버 ID", example = "310") @PathVariable Long memberId,
      @Parameter(description = "조회할 연도 (예: 2025)", example = "2025") @RequestParam(required = false) Integer year,
      @Parameter(description = "조회할 월 (1-12)", example = "8") @RequestParam(required = false) Integer month) {
    return ResponseEntity.ok(rankingService.getMemberRank(memberId, year, month));
  }

  @Operation(summary = "내 랭킹 조회 (월간)", description = "요청을 보낸 유저의 월간 개인 랭킹과 점수를 조회합니다.")
  @GetMapping("/members/me")
  public ResponseEntity<RankingDto> getMyRank(
          @AuthenticationPrincipal CustomUserDetails customUserDetails,
          @Parameter(description = "조회할 연도 (예: 2025)", example = "2025") @RequestParam(required = false) Integer year,
          @Parameter(description = "조회할 월 (1-12)", example = "8") @RequestParam(required = false) Integer month) {
    Long memberId = customUserDetails.getId();

    return ResponseEntity.ok(rankingService.getMemberRank(memberId, year, month));
  }

  @Operation(summary = "팀간 랭킹 조회 (월간)", description = "전체 팀들의 월간 랭킹을 조회합니다.")
  @GetMapping("/teams/top")
  public ResponseEntity<List<TeamRankingDto>> getInterTeamRanking(
      @Parameter(description = "조회할 팀 수", example = "10") @RequestParam(defaultValue = "10") int limit,
      @Parameter(description = "조회할 연도 (예: 2025)", example = "2025") @RequestParam(required = false) Integer year,
      @Parameter(description = "조회할 월 (1-12)", example = "8") @RequestParam(required = false) Integer month) {
    return ResponseEntity.ok(rankingService.getInterTeamRanking(limit, year, month));
  }

  @Operation(summary = "팀 내부 멤버 랭킹 조회 (주간)", description = "특정 팀에 소속된 멤버들의 주간 랭킹을 조회합니다.")
  @GetMapping("/teams/{teamId}/members")
  public ResponseEntity<List<RankingDto>> getIntraTeamRanking(
      @Parameter(description = "팀 ID", example = "1") @PathVariable Long teamId,
      @Parameter(description = "조회할 멤버 수", example = "10") @RequestParam(defaultValue = "10") int limit,
      @Parameter(description = "조회할 연도 (예: 2025)", example = "2025") @RequestParam(required = false) Integer year,
      @Parameter(description = "조회할 주차 (1-53)", example = "34") @RequestParam(required = false) Integer week) {
    return ResponseEntity.ok(rankingService.getIntraTeamRanking(teamId, limit, year, week));
  }

  // --- 관리용 API ---

  @DeleteMapping("/members")
  @Operation(summary = "[관리] 개인 랭킹 초기화", description = "지정된 년/월의 전체 개인 랭킹을 삭제합니다.")
  public ResponseEntity<Void> clearMemberRanking(
      @Parameter(description = "초기화할 연도", required = true, example = "2025") @RequestParam Integer year,
      @Parameter(description = "초기화할 월", required = true, example = "08") @RequestParam Integer month) {
    // [변경] 서비스 호출 메서드명 변경
    rankingService.clearMemberRanking(year, month);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/teams")
  @Operation(summary = "[관리] 팀간 랭킹 초기화", description = "지정된 년/월의 전체 팀간 랭킹을 삭제합니다.")
  public ResponseEntity<Void> clearInterTeamRanking(
      @Parameter(description = "초기화할 연도", required = true, example = "2025") @RequestParam Integer year,
      @Parameter(description = "초기화할 월", required = true, example = "08") @RequestParam Integer month) {
    rankingService.clearInterTeamRanking(year, month);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/teams/{teamId}")
  @Operation(summary = "[관리] 팀 내부 랭킹 초기화", description = "지정된 년/주의 특정 팀 내부 랭킹을 삭제합니다.")
  public ResponseEntity<Void> clearIntraTeamRanking(
      @Parameter(description = "초기화할 팀 ID") @PathVariable Long teamId,
      @Parameter(description = "초기화할 연도", required = true, example = "2025") @RequestParam Integer year,
      @Parameter(description = "초기화할 주차", required = true, example = "08") @RequestParam Integer week) {
    rankingService.clearIntraTeamRanking(teamId, year, week);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/teams/{teamId}/all")
  @Operation(summary = "[관리] 특정 팀의 모든 랭킹 데이터 삭제", description = "팀 삭제 시 호출. 해당 팀의 모든 실시간 랭킹 데이터를 Redis에서 삭제합니다.")
  public ResponseEntity<Void> deleteTeamRanking(
      @Parameter(description = "삭제할 팀 ID") @PathVariable Long teamId) {
    rankingService.deleteTeamRanking(teamId);
    return ResponseEntity.ok().build();
  }
}