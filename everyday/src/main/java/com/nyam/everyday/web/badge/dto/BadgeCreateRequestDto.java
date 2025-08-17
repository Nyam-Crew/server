package com.nyam.everyday.web.badge.dto;

import com.nyam.everyday.module.badge.entity.BadgeType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "뱃지 생성 요청 정보")
public class BadgeCreateRequestDto {
    @Schema(description = "뱃지 이름", example = "성실회원")
    private String name;

    @Schema(description = "뱃지 설명", example = "10일 연속 출석한 우수회원에게 부여되는 뱃지입니다.")
    private String description;

    @Schema(description = "뱃지 타입 (REGULAR_CHALLENGE, EVENT_CHALLENGE)", example = "REGULAR_CHALLENGE")
    private BadgeType badgeType;
}
