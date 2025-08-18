package com.nyam.everyday.module.badge.repository;

import com.nyam.everyday.module.badge.dto.OwnedBadgeDto;
import com.nyam.everyday.module.badge.entity.Badge;
import com.nyam.everyday.module.badge.entity.MemberBadgeStatus;
import com.nyam.everyday.module.member.entity.Member;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberBadgeStatusRepository extends JpaRepository<MemberBadgeStatus, Long> {

    Optional<MemberBadgeStatus> findByMemberAndBadge(Member member, Badge badge);

    @Query("""
    select mbs.badge.id as badgeId, mbs.createdDate as acquiredAt
    from MemberBadgeStatus mbs
    where mbs.member.memberId = :memberId
      and mbs.badge.id in :badgeIds
    """)
    List<OwnedBadgeDto> findOwnedBadgeProjections(
        @Param("memberId") Long memberId,
        @Param("badgeIds") Collection<Long> badgeIds
    );
}
