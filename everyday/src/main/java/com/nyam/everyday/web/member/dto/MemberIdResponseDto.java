package com.nyam.everyday.web.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 피드와 채팅 프론트 구현에 필요한 로그인한 사용자 아이디 Dto
 *
 * @author : 이지은
 * @fileName : MemberIdResponseDto
 * @since : 25. 8. 25.
 *
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberIdResponseDto {
    private Long memberId;
}