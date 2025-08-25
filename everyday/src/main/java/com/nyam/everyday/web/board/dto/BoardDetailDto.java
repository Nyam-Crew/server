package com.nyam.everyday.web.board.dto;

import com.nyam.everyday.module.board.entity.Board;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BoardDetailDto", description = "게시글 상세 응답 DTO (본문 + 메타 정보). 댓글은 별도 API로 페이징 조회")
public class BoardDetailDto {

  @Schema(description = "게시글 ID", example = "123")
  private Long boardId;

  @Schema(description = "작성자 닉네임", example = "hyunb")
  private String nickname;

  @Schema(description = "게시글 제목", example = "첫 글입니다! 다이어트 꿀팁 공유")
  private String boardTitle;

  @Schema(description = "게시글 본문(HTML/Markdown 가능)", example = "<p>안녕하세요! 오늘 식단은…</p>")
  private String boardContent;

  @Schema(description = "게시판 타입", example = "RECIPE")
  private String boardType;

  @Schema(description = "작성일시", type = "string", format = "date-time", example = "2025-08-24T12:34:56")
  private LocalDateTime createdDate;

  @Schema(description = "조회수", example = "42")
  private Long viewCount;

  @Schema(description = "좋아요 수", example = "7")
  private Long likeCount;

  @Schema(description = "댓글 수(루트+대댓글 합계)", example = "3")
  private Long commentCount;

  @Schema(description = "요청 사용자가 좋아요를 눌렀는지 여부(미인증/비적용 시 null)", example = "false", nullable = true)
  private Boolean likedByMe;

  /** 인증 사용자 좋아요 여부까지 포함해서 변환 */
  public static BoardDetailDto from(Board e, Boolean likedByMe) {
    return BoardDetailDto.builder()
        .boardId(e.getBoardId())
        .nickname(e.getMember() != null ? e.getMember().getNickname() : null)
        .boardTitle(e.getBoardTitle())
        .boardContent(e.getBoardContent())
        .boardType(e.getBoardType())
        .createdDate(e.getCreatedDate())
        .viewCount(e.getViewCount())
        .likeCount(e.getLikeCount())
        .commentCount(e.getCommentCount())
        .likedByMe(likedByMe)
        .build();
  }

  /** likedByMe 판단이 필요 없을 때(오버로드) */
  public static BoardDetailDto from(Board e) {
    return from(e, null);
  }
}