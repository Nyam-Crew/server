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
@EntityListeners(AuditingEntityListener.class)
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


}