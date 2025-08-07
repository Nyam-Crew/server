package com.nyam.everyday.web.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 그룹 CRUD DTO
 *
 * @author : 이지은
 * @fileName : TeamDTO
 * @since : 25. 8. 4.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDto {
    @Schema(name = "그룹 ID", example = "1")
    private Long teamId;

    @Schema(description = "그룹 제목", example = "운동하는 사자들")
    private String teamTitle;

    @Schema(description = "그룹 설명", example = "매일 운동 인증하는 그룹입니다.")
    private String teamDescription;

    @Schema(description = "대표 이미지 URL (이미지 업로드 안 할 경우)", example = "https://example.com/image.jpg")
    private String teamImg;

    @Schema(description = "현재 인원 수", example = "1")
    private int teamCurrentMembers;

    @Schema(description = "최대 인원 수", example = "10")
    private int teamMaxMembers;

    @Schema(name = "그룹 생성일자")
    private LocalDateTime createdDate;

    @Schema(name = "그룹 수정일자")
    private LocalDateTime modifiedDate;

    @Schema(description = "그룹 생성자(방장) ID", example = "1")
    private Long ownerId;
}