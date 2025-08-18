package com.nyam.everyday.web.badge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AssignBadgeRequestDto {

    @Schema(description = "뱃지 ID", example = "1")
    private Long badgeId;
}