package com.nyam.everyday.web.badge.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyam.everyday.module.badge.entity.Badge;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class BadgeOwnershipDto {

  @Schema(description = "뱃지 ID")
  private Long id;

  @Schema(description = "뱃지 이름")
  private String name;

  @Schema(description = "뱃지 설명")
  private String description;

  @Schema(description = "뱃지 이미지 URL")
  private String badgeImage;

  @JsonProperty("isOwned")
  @Schema(description = "뱃지 보유 여부")
  private boolean isOwned;

  @Schema(description = "뱃지 생성 날짜")
  private LocalDateTime createdDate;

  @Schema(description = "뱃지 획득 날짜")
  private LocalDateTime acquiredAt;


  public BadgeOwnershipDto(Badge badge, boolean owned, LocalDateTime acquiredAt) {
    this.id = badge.getId();
    this.name = badge.getName();
    this.description = badge.getDescription();
    this.badgeImage = badge.getBadgeImage();
    this.isOwned = owned;
    this.createdDate = badge.getCreatedDate();
    this.acquiredAt = owned ? acquiredAt : null;
  }
}
