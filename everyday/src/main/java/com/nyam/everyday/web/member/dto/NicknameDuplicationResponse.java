package com.nyam.everyday.web.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "닉네임 중복 확인 응답")
public class NicknameDuplicationResponse {

  @Schema(description = "닉네임 중복 여부", example = "true")
  private boolean isDuplicate;
}