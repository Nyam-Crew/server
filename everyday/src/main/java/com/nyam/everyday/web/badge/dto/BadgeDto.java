package com.nyam.everyday.web.badge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "뱃지 정보")
public class BadgeDto {

    @Schema(description = "뱃지 이름")
    private String name;

    @Schema(description = "뱃지 설명")
    private String description;

    @Schema(description = "뱃지 이미지 파일 - 없으면 기본 이미지로 생성")
    private MultipartFile badgeImageFile;

    @Schema(description = "뱃지 이미지 url (최조 등록시에는 입력안함_S3에 저장된 이미지 url 리턴)")
    private String badgeImage;
}