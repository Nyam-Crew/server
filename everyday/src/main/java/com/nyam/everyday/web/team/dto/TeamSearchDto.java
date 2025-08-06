package com.nyam.everyday.web.team.dto;

import lombok.*;

/**
 * 그룹 검색 확장을 염두에 둔 관련 Dto
 * Todo. 추후 Elastic Search  확장 대비하여 작성만!
 *
 * @author : 이지은
 * @fileName : TeamSearchDTO
 * @since : 25. 8. 6.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamSearchDto {
    private String keyword;
    private String category; // (추후 사용 예정)
    private String sort;     // (createdAt, 인기순 등)
}