package com.nyam.everyday.web.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 그룹 상세 조회 DTO
 *
 * @author : 이지은
 * @fileName : TeamDetailDto
 * @since : 25. 8. 6.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDetailDto {

    @Schema(name = "", example = "")
    private Long teamId;

    @Schema(name = "", example = "")
    private String teamTitle;

    @Schema(name = "", example = "")
    private String teamDescription;

    @Schema(name = "", example = "")
    private String teamImage;

    @Schema(name = "", example = "")
    private int maxMembers;

    @Schema(name = "", example = "")
    private int currentMemberCount;

    @Schema(name = "", example = "")
    private String createdDate;

    @Schema(name = "", example = "")
    private ParticipationStatus status;

    @Schema(name = "", example = "")
    private TeamRole teamRole;

    public enum ParticipationStatus {
        NOT_JOINED,    // 가입 요청 안함
        PENDING,       // 가입 요청 중
        APPROVED,      // 가입 완료
        REJECTED,      // 거절됨
        BANNED         // 강퇴됨
    }

    public enum TeamRole {
        LEADER,
        SUBLEADER,
        MEMBER
    }
}