package com.nyam.everyday.web.boardlike.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class BoardLikeRequestDto {
  @NotNull
  private Long boardId;
}
