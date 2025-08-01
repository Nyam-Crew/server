package com.nyam.everyday.web.user.controller;

import com.nyam.everyday.web.user.dto.MemberDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "User-Controller", description = "회원 관리")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class MemberController {

  @Operation(summary = "회원 정보", description = "로그인한 회원의 정보를 조회합니다.")
  @GetMapping
  public ResponseEntity<MemberDto> getUserInfo() {
    log.info("회원 정보 호출! ");
    return null;
  }
}
