package com.nyam.everyday.web.team.controller;

import com.nyam.everyday.module.team.service.TeamSearchQueryService;
import com.nyam.everyday.module.team.service.TeamSearchService;
import com.nyam.everyday.module.team.service.TeamService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.team.dto.TeamDetailDto;
import com.nyam.everyday.web.team.dto.TeamDto;
import com.nyam.everyday.web.team.dto.TeamSearchDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    private final TeamSearchQueryService teamSearchService;


    @Operation(summary = "그룹 생성", description = "그룹을 생성합니다. swagger에서는 이미지랑 테스트하기가 빡세서 주석처리해두었습니다. 이미지 제외하고 테스트 완료")
    @PostMapping
    public ResponseEntity<TeamDto> createTeam(
            @RequestBody TeamDto teamDTO,
            //@RequestPart(required = false) MultipartFile imageFile,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        //파라미터 그룹 이름, 그룹 설명, 그룹 이미지, 최대인원수(최소인원수==2), memberId 받아서 해당 ID를 owner(방장)으로
        Long memberId = userDetails.getId(); // 인증된 사용자로부터 방장 ID 추출

        TeamDto response = teamService.createTeam(teamDTO, /*imageFile,*/ memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "그룹 조회", description = "그룹 리스트를 조회합니다.")
    @GetMapping
    public Page<TeamDto> getTeamList(
            @RequestParam(required = false) String keyword,
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable){
        return teamService.getTeamList(keyword, pageable);
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


}