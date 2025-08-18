package com.nyam.everyday.module.badge.entity;

import com.nyam.everyday.common.entity.BaseCreatedEntity;
import com.nyam.everyday.module.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_badge_status")
public class MemberBadgeStatus extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mbs_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    public MemberBadgeStatus(Member member, Badge badge) {
        this.member = member;
        this.badge = badge;
    }
}
