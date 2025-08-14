package com.nyam.everyday.web.member.controller;

import com.nyam.everyday.common.dto.CustomPageResponseDto;
import com.nyam.everyday.module.badge.service.BadgeService;
import com.nyam.everyday.module.member.service.MemberService;
import com.nyam.everyday.module.team.service.TeamMemberService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.badge.dto.AssignBadgeRequestDto;
import com.nyam.everyday.web.badge.dto.BadgeOwnershipDto;
import com.nyam.everyday.web.member.dto.MemberDto;
import com.nyam.everyday.web.member.dto.NicknameDuplicationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Member-Controller", description = "회원 관리")
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;
  private final BadgeService badgeService;

  @GetMapping("/{memberId}")
  @Operation(summary = "회원 정보", description = "회원의 정보를 조회합니다.")
  public ResponseEntity<MemberDto> getMember(@PathVariable Long memberId) {
    MemberDto memberDto = memberService.getMemberById(memberId);
    return ResponseEntity.ok(memberDto);
  }

  @GetMapping("/me")
  @Operation(summary = "로그인한 회원 정보", description = "로그인한 회원의 정보를 조회합니다.")
  public ResponseEntity<MemberDto> getMember(@AuthenticationPrincipal CustomUserDetails userDetails) {
    Long id = userDetails.getId();
    log.info("[getMember] memberId : {}", id);
    return ResponseEntity.ok(memberService.getMemberById(id));
  }

  @PostMapping
  @Operation(summary = "회원 추가", description = "회원을 신규 추가합니다.")
  public ResponseEntity<MemberDto> create(@RequestBody MemberDto dto) {
    MemberDto saved = memberService.create(dto);
    return ResponseEntity.ok(saved);
  }

  @PutMapping("/{memberId}")
  @Operation(summary = "회원 정보 수정", description = "회원 정보를 수정합니다.")
  public ResponseEntity<MemberDto> update(@PathVariable Long memberId, @RequestBody MemberDto dto) {
    MemberDto updated = memberService.update(memberId, dto);
    return ResponseEntity.ok(updated);
  }

  @GetMapping("/check-nickname/{nickname}")
  @Operation(summary = "닉네임 중복 확인", description = "닉네임의 중복 여부를 확인합니다.")
  public ResponseEntity<NicknameDuplicationResponse> checkNicknameDuplication(@PathVariable String nickname) {
    NicknameDuplicationResponse response = memberService.checkNicknameDuplication(nickname);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{memberId}/badges")
  @Operation(summary = "회원에게 뱃지 부여", description = "특정 회원에게 뱃지를 부여합니다.")
  public ResponseEntity<Void> assignBadgeToMember(
      @PathVariable Long memberId,
      @RequestBody AssignBadgeRequestDto requestDto) {
    log.info("[assignBadgeToMember] memberId: {}, badgeId: {}", memberId, requestDto.getBadgeId());
    badgeService.assignBadgeToMember(memberId, requestDto);
    return ResponseEntity.ok().build();
  }


  /**
   * 페이징 입력 형식
   * {
   *   "page": 0,
   *   "size": 9,
   *   "sort":
   *     "createdDate"
   * }
   * */
  @GetMapping("/my-badges")
  @Operation(summary = "뱃지 목록 조회 (페이지네이션)", description = "페이지네이션된 뱃지 목록을 조회합니다. 현재 사용자의 소유 여부도 포함됩니다.")
  public ResponseEntity<CustomPageResponseDto<BadgeOwnershipDto>> getBadges(
      @PageableDefault(size = 9, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    log.info("[getBadges] Pageable request: {}", pageable);

    Long currentUserId = (userDetails != null) ? userDetails.getId() : null;
    log.info("[getBadges] 요청한 User ID: {}", currentUserId);

    Page<BadgeOwnershipDto> response = badgeService.getBadgeListWithOwnership(pageable, currentUserId);
    return ResponseEntity.ok(new CustomPageResponseDto<>(response));
  }
}
