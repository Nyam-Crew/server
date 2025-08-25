package com.nyam.everyday.web.team.controller;

import com.nyam.everyday.module.team.service.*;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.team.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import java.util.List;

/**
 *
 * 그룹 CRUD 관련 컨트롤러
 * @fileName        : TeamController
 * @author          : 이지은
 * @since           : 25. 8. 5.
 *
 */
@Tag(name="Team-Controller", description = "그룹의 전반적인 기본 흐름을 확인할 수있는 컨트롤러입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;
    private final TeamMemberService teamMemberService;
    private final TeamImageService teamImageService;

    //private final TeamSearchQueryService teamSearchService;
    //private final HandlerMapping resourceHandlerMapping;


    @Operation(summary = "그룹 생성", description = "그룹을 생성합니다. swagger에서는 이미지랑 테스트하기가 빡세서 주석처리해두었습니다. 이미지 제외하고 테스트 완료")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TeamDto> createTeam(
            @RequestPart("dto") @Valid TeamDto teamDTO,
            // 3. 이미지 파일 부분을 주석 해제합니다. "imageFile"은 이미지 파일을 부를 이름(key)입니다.
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        //파라미터 그룹 이름, 그룹 설명, 그룹 이미지, 최대인원수(최소인원수==2), memberId 받아서 해당 ID를 owner(방장)으로
        Long memberId = userDetails.getId(); // 인증된 사용자로부터 방장 ID 추출

        TeamDto response = teamService.createTeam(teamDTO, imageFile, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "그룹 조회", description = "그룹 리스트를 조회합니다. 검색, 정렬, 필터링을 지원합니다.")
    @GetMapping
    public Page<TeamDto> getTeamList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "latest") String sort, // 정렬 파라미터 추가 (기본값 '최신순')
            @RequestParam(required = false, defaultValue = "false") boolean availableOnly, // 참가 가능 그룹만 볼지 여부 파라미터 추가
            @ParameterObject Pageable pageable){

        // 서비스 계층으로 모든 파라미터를 넘겨줍니다.
        return teamService.getTeamList(keyword, sort, availableOnly, pageable);
    }

    // TODO: 추후 ElasticSearch 확장 대비하여 검색 조건 분리 설계
    // ↓ 아래 방식으로 리팩토링할 수 있음
    /*
    @GetMapping("/teams")
    public Page<TeamDto> getTeamList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        TeamSearchDto condition = TeamSearchDto.builder()
                .keyword(keyword)
                .category(category)
                .sort(sort)
                .build();

        return teamSearchService.searchTeams(condition, pageable);
    }
    */

    //@Operation(summary = "", description = "")
    @Operation(summary = "그룹 정보 상세 조회", description = "사용자가 선택(클릭)한 그룹의 상세정보를 조회해옵니다.")
    @GetMapping("/{teamId}")
    public ResponseEntity<TeamDetailDto> getTeam(
            @PathVariable Long teamId,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        Long memberId = userDetails.getId();
        TeamDetailDto teamDetail = teamService.getTeam(teamId, memberId);
        return ResponseEntity.ok(teamDetail);
    }


    @Operation(summary = "그룹 참가 신청", description = "특정 그룹에 참가 신청을 보냅니다.")
    @PostMapping("/{teamId}/join")
    public ResponseEntity<String> requestToJoinTeam(@PathVariable Long teamId,
                                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        teamService.requestToJoin(teamId, userDetails.getId());
        return ResponseEntity.ok("참가 신청이 완료되었습니다.");
    }

    @Operation(summary = "그룹 참가 신청 취소", description = "보냈던 참가 신청을 취소합니다.")
    @DeleteMapping("/{teamId}/join")
    public ResponseEntity<String> cancelJoinRequest(
            @PathVariable Long teamId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        teamService.cancelJoinRequest(teamId, userDetails.getId());
        return ResponseEntity.ok("참가 신청이 취소되었습니다.");
    }

    @Operation(summary = "그룹 참가 신청 중인 유저 목록", description = "그룹에 참가 신청 중인 유저 목록을 조회")
    @GetMapping("/{teamId}/join-requests")
    public ResponseEntity<List<TeamMemberStatusDto>> getJoinRequests(
            @PathVariable Long teamId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<TeamMemberStatusDto> result = teamService.getJoinRequestMembers(teamId, userDetails.getId());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "그룹 참가 승인/거부", description = "그룹에 신청된 참가 신청을 승인/거부")
    @PatchMapping("/{teamId}/members/{memberId}/status")
    public ResponseEntity<Void> updateMemberStatus(
            @PathVariable Long teamId,
            @PathVariable Long memberId,
            @RequestBody MemberStatusUpdateDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        teamMemberService.updateMemberStatus(teamId, memberId, request.getStatus(), userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "그룹 멤버 목록 조회", description = "그룹에 속한 멤버들의 목록과 역할을 조회")
    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<TeamMemberDTO>> getTeamMembers(@PathVariable Long teamId) {
        List<TeamMemberDTO> members = teamService.getApprovedTeamMembers(teamId);
        return ResponseEntity.ok(members);
    }


    @Operation(summary = "그룹 기본정보 수정", description = "제목/설명/최대인원 부분 업데이트")
    @PatchMapping("/{teamId}")
    public ResponseEntity<TeamDetailDto> updateTeam(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamUpdateDto request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        TeamDetailDto dto = teamService.updateTeam(teamId, user.getId(), request);
        return ResponseEntity.ok(dto);
    }


    @Operation(summary = "그룹 이미지 교체", description = "S3 업로드 후 teamImg 갱신")
    @PostMapping(path = "/{teamId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TeamDetailDto> updateTeamImage(
            @PathVariable Long teamId,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        TeamDetailDto dto = teamImageService.updateTeamImage(teamId, user.getId(), file);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "그룹 하드 삭제(방장 전용)", description = "확인용 팀명이 정확히 일치해야 삭제됩니다. 삭제 시 복구할 수 없습니다.")
    @DeleteMapping(value = "/{teamId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteTeam(
            @PathVariable Long teamId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody TeamDeleteDto deleteTitle
    ) {
        teamService.deleteTeamHard(teamId, userDetails.getId(), deleteTitle.getConfirmTeamTitle());
        return ResponseEntity.noContent().build();
    }

}