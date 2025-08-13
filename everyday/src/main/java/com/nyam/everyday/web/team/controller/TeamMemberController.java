package com.nyam.everyday.web.team.controller;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.team.service.TeamMemberService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.team.dto.TeamBanDto;
import com.nyam.everyday.web.team.dto.TeamRoleChangeDto;
import com.nyam.everyday.web.team.dto.TeamTransLeaderDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getId();

        teamMemberService.leaveTeam(teamId, memberId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("그룹에서 성공적으로 탈퇴되었습니다.");
    }

    @Operation(summary = "그룹 강퇴", description = "방장이 멤버를 그룹에서 강퇴(BANNED)시킵니다.")
    @PostMapping("/{teamId}/members/{targetMemberId}/ban")
    public ResponseEntity<String> banMember(
            @PathVariable Long teamId,
            @PathVariable Long targetMemberId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody(required = false) TeamBanDto request
    ) {
        if (userDetails == null) {
            throw new BaseException(ErrorCode.AUTHENTICATION_FAILED); // 401
        }
        Long memberId = userDetails.getId();

        String reason = null;
        if (request != null) {
            reason = request.getReason();
        }
        teamMemberService.ban(teamId, memberId, targetMemberId, reason);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("해당 멤버를 강퇴했습니다.");
    }

    @Operation(summary = "방장 권한 위임", description = "리더가 특정 멤버에게 방장 권한을 위임합니다.")
    @PatchMapping("/teams/{teamId}/leader")
    public ResponseEntity<String> transferLeader(
            @PathVariable Long teamId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody TeamTransLeaderDto req
    ) {
        teamMemberService.transferLeader(teamId, userDetails.getId(), req.getTargetMemberId());
        return ResponseEntity.ok("방장 권한이 위임되었습니다.");
    }

    @Operation(summary = "부방장 권한 부여/회수", description = "리더가 멤버의 역할을 SUBLEADER 또는 MEMBER로 변경합니다.")
    @PatchMapping("/teams/{teamId}/role")
    public ResponseEntity<String> changeRole(
            @PathVariable Long teamId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody TeamRoleChangeDto req
    ) {
        teamMemberService.changeRole(teamId, userDetails.getId(), req.getTargetMemberId(), req.getRole());
        return ResponseEntity.ok("역할을 변경했습니다.");
    }

}