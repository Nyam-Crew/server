package com.nyam.everyday.web.team.dto;

import com.nyam.everyday.module.team.enums.TeamNoticeType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 그룹 공지 DTO
 *
 * @author : 이지은
 * @fileName : TeamNoticeDTO
 * @since : 25. 8. 4.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamNoticeDto {

    @Schema(name = "그룹 공지 ID", example = "1")
    private Long teamNoticeId;

    @Schema(name = "그룹 ID", example = "1")
    private Long teamId;

    @Schema(name = "멤버 ID", example = "1")
    private Long memberId;

    @Schema(name = "공지 타이틀", example = "공지입니다")
    private String title;

    @Schema(name = "공지 콘텐츠", example = "내일이면 8월 그룹 챌린지가 시작됩니다! 화이팅")
    private String content;

    @Schema(name = "공지 타입", example = "FIXED / NORMAL")
    private TeamNoticeType teamNoticeType;

    @Schema(name = "그룹공지 생성 시간")
    private LocalDateTime createdDate;

    @Schema(name = "그룹공지 수정 시간")
    private LocalDateTime modifiedDate;
}