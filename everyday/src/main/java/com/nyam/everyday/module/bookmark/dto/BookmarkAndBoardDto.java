package com.nyam.everyday.module.bookmark.dto;

import java.time.LocalDateTime;

public record BookmarkAndBoardDto(
    Long boardId,
    Long bookmarkId,
    String boardTitle,
    String boardContent,
    String boardAuthorNickname,
    Long likeCount,
    Long viewCount,
    Long commentCount,
    String boardType,
    LocalDateTime bookmarkedAt
) {}