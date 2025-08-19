package com.nyam.everyday.web.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 * 커서 응답 담을 DTO
 *
 * @author : 이지은
 * @fileName : FeedSlice
 * @since : 25. 8. 18.
 *
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class FeedSlice {

    @Schema(name = "최신순 아이템 리스트")
    private final List<TeamActivityFeedItem> items;

    @Schema(name= "다음 페이지 조회용 커서")
    private final Long nextCursorEpochMs;

    @Schema(name="다음 페이지 여부")
    private final boolean hasNext;

}