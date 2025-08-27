package com.nyam.everyday.web.team.dto;

import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.module.team.enums.ParticipationStatus;
import com.nyam.everyday.module.team.enums.TeamRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 *
 * 그룹 멤버 목록 조회
 *
 * @author : 이지은
 * @fileName : TeamMemberDTO
 * @since : 25. 8. 7.
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberDTO {

    @Schema(description = "유저 ID", example = "1")
    private Long memberId;

    @Schema(description = "그룹 참여한 유저 닉네임", example = "까꿍")
    private String nickname;

    @Schema(description = "유저 프로필 이미지 URL", example = "https://s3.bucket/member/profile.jpg")
    private String memberImg;

    @Schema(description = "그룹 내 유저 권한", example = "MEMBER")
    private TeamRole teamRole;

    @Schema(name = "그룹 참여 상태", example = "APPROVED")
    private ParticipationStatus status;

    @Schema(name="그룹 신청 날짜", example = "그룹 가입 신청 일자")
    private LocalDateTime joinedDate;
}