package com.nyam.everyday.web.team.controller;

import com.nyam.everyday.module.team.service.TeamMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 그룹 멤버 관리에 대한 컨트롤러
 *
 * @author : 이지은
 * @fileName : TeamMemberController
 * @since : 25. 8. 5.
 */

@Tag(name="TeamMember-Controller", description = "그룹 멤버 관리에 대한 컨트롤러입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    @Operation(summary = "그룹 나가기", description = "본인이 가입한 그룹에서 탈퇴합니다.")
    @PatchMapping("/{teamId}/leave/me")
    public ResponseEntity<String> leaveTeam(
            @PathVariable Long teamId,
            @AuthenticationPrincipal Long memberId
    ) {
        teamMemberService.leaveTeam(teamId, memberId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("그룹에서 성공적으로 탈퇴되었으며, 참여 기록이 삭제되었습니다.");
    }

}