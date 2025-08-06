package com.nyam.everyday.web.team.controller;

import com.nyam.everyday.module.team.service.TeamService;
import com.nyam.everyday.web.team.dto.TeamDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @Operation(summary = "그룹 생성", description = "그룹을 생성합니다.")
    @PostMapping
    public ResponseEntity<TeamDto> createdTeam(@RequestBody TeamDto teamDTO, @AuthenticationPrincipal CustomUserDetails userDetails) {
        //파라미터 그룹 이름, 그룹 설명, 그룹 이미지, 최대인원수(최소인원수==2), memberId 받아서 해당 ID를 owner(방장)으로
        Long memberId = userDetails.getMemberId(); // 인증된 사용자로부터 방장 ID 추출

        TeamDto response = teamService.createTeam(teamDTO, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


}