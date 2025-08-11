package com.nyam.everyday.web.team.controller;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.team.service.TeamMemberService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.team.dto.TeamBanDto;
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

}