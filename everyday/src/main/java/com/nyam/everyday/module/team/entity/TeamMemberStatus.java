package com.nyam.everyday.module.team.entity;

import com.nyam.everyday.common.entity.BaseEntity;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.team.enums.ParticipationStatus;
import com.nyam.everyday.module.team.enums.TeamRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * 그룹 참여 현황 entity
 *
 * @author : 이지은
 * @fileName : teamMemberStatus
 * @since : 25. 8. 4.
 */
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "team_member_status")
public class TeamMemberStatus extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_member_id")
    private Long teamMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private ParticipationStatus status;

    @Column(name = "team_role", length = 10)
    @Enumerated(EnumType.STRING)
    private TeamRole teamRole; // 예: MEMBER, LEADER

    @Column(name="banned_reason", nullable = true)
    private String bannedReason;

    @Column(name="banned_date", nullable = true)
    private LocalDateTime bannedDate;

    @Column(name="left_date", nullable = true)
    private LocalDateTime leftDate;

    public void approve() {
        if (this.status != ParticipationStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 요청입니다.");
        }
        this.status = ParticipationStatus.APPROVED;
    }

    public void reject() {
        if (this.status != ParticipationStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 요청입니다.");
        }
        this.status = ParticipationStatus.REJECTED;
    }

    /** 탈퇴 처리: 상태/시간/역할 초기화까지 한 번에 */
    public void markLeft(LocalDateTime when) {
        this.status = ParticipationStatus.LEFT;
        this.leftDate = when;
        //this.teamRole = TeamRole.MEMBER;
    }

    public void ban(String reason, LocalDateTime when) {
        this.status = ParticipationStatus.BANNED;
        this.bannedReason = (reason == null || reason.isBlank()) ? null : reason.trim();
        this.bannedDate = when;
        // 필요 시 역할 초기화: this.teamRole = null; 지금 nullable = false라서 활성화하면 에러발생
    }

    public void changeRole(TeamRole newRole) {
        this.teamRole = newRole;
    }

}