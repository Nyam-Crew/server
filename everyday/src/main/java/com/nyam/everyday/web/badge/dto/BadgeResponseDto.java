package com.nyam.everyday.web.badge.dto;

import com.nyam.everyday.module.badge.entity.BadgeType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "뱃지 정보")
public class BadgeResponseDto {

    @Schema(description = "뱃지 이름", example = "우수회원")
    private String name;

    @Schema(description = "뱃지 설명", example = "10일 연속 출석한 우수회원에게 부여되는 뱃지입니다.")
    private String description;

    @Schema(description = "뱃지 타입 (REGULAR_CHALLENGE, EVENT_CHALLENGE)", example = "REGULAR_CHALLENGE")
    private BadgeType badgeType;

    @Schema(description = "뱃지 이미지 url (최조 등록시에는 입력안함_S3에 저장된 이미지 url 리턴)", example = "https://s3.amazonaws.com/badges/badge123.png")
    private String badgeImage;

    @Schema(description = "뱃지 생성 날짜")
    private LocalDateTime createdDate;
}
