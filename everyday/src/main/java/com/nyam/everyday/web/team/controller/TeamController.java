package com.nyam.everyday.web.team.controller;

import com.nyam.everyday.module.team.service.TeamSearchQueryService;
import com.nyam.everyday.module.team.service.TeamSearchService;
import com.nyam.everyday.module.team.service.TeamService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.team.dto.TeamDto;
import com.nyam.everyday.web.team.dto.TeamSearchDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 *
 * 그룹 CRUD 관련 컨트롤러 
 * @fileName        : TeamController
 * @author          : 이지은
 * @since           : 25. 8. 5.
 * 
 */
@Tag(name="", description = "")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;
    private final TeamSearchQueryService teamSearchService;

    @Operation(summary = "그룹 생성", description = "그룹을 생성합니다.")
    @PostMapping
    public ResponseEntity<TeamDto> createTeam(@RequestBody TeamDto teamDTO, @AuthenticationPrincipal CustomUserDetails userDetails) {
        //파라미터 그룹 이름, 그룹 설명, 그룹 이미지, 최대인원수(최소인원수==2), memberId 받아서 해당 ID를 owner(방장)으로
        Long memberId = userDetails.getId(); // 인증된 사용자로부터 방장 ID 추출

        TeamDto response = teamService.createTeam(teamDTO, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //@Operation(summary = "", description = "")
    @GetMapping
    public Page<TeamDto> getTeamList(
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable){
        return teamService.getTeamList(keyword, pageable);
    }

    // TODO: 추후 ElasticSearch 확장 대비하여 검색 조건 분리 예정
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


}