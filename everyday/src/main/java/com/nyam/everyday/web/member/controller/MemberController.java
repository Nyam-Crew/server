package com.nyam.everyday.web.member.controller;

import com.nyam.everyday.module.member.service.MemberService;
import com.nyam.everyday.module.team.service.TeamMemberService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.member.dto.MemberDto;
import com.nyam.everyday.web.team.dto.MemberTeamListDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final TeamMemberService teamMemberService;

  @GetMapping("/{id}")
  @Operation(summary = "회원 정보", description = "회원의 정보를 조회합니다.")
  public ResponseEntity<MemberDto> getMember(@PathVariable Long id) {
    MemberDto memberDto = memberService.getMemberById(id);
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

  @PutMapping("/{id}")
  @Operation(summary = "회원 정보 수정", description = "회원 정보를 수정합니다.")
  public ResponseEntity<MemberDto> update(@PathVariable Long id, @RequestBody MemberDto dto) {
    MemberDto updated = memberService.update(id, dto);
    return ResponseEntity.ok(updated);
  }

}
