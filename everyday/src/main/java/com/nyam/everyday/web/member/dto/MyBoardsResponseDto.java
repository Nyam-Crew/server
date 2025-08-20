package com.nyam.everyday.web.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "내 게시글 정보 응답")
public class MyBoardsResponseDto {

    @Schema(description = "게시글 ID")
    private Long boardId;

    @Schema(description = "게시글 제목")
    private String boardTitle;

    @Schema(description = "작성자 닉네임")
    private String nickname;

    @Schema(description = "게시글 종류")
    private String boardType;

    @Schema(description = "작성일", example = "2025-01-01T00:00:00")
    private LocalDateTime createdDate;

    @Schema(description = "조회수")
    private Long viewCount;

    @Schema(description = "댓글 수")
    private Long commentCount;

    @Schema(description = "좋아요 수")
    private Long likeCount;
}