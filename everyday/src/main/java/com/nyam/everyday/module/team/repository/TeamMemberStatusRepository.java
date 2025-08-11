package com.nyam.everyday.module.team.repository;

import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.module.team.enums.ParticipationStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 그룹 멤버 현황 관련 Repository
 *
 * @author : 이지은
 * @fileName : TeamMemberStatusRepository
 * @since : 25. 8. 6.
 */
@Repository
public interface TeamMemberStatusRepository extends JpaRepository<TeamMemberStatus, Long> {

    Optional<TeamMemberStatus> findByTeam_TeamIdAndMember_MemberId(Long teamId, Long memberId);

    List<TeamMemberStatus> findAllByTeam_TeamIdAndStatus(Long teamId, ParticipationStatus participationStatus);

    @Query("SELECT tms FROM TeamMemberStatus tms JOIN FETCH tms.member WHERE tms.team.teamId = :teamId AND tms.status = :status")
    List<TeamMemberStatus> findAllWithMemberByTeam_TeamIdAndStatus(@org.springframework.data.repository.query.Param("teamId") Long teamId, @Param("status") ParticipationStatus status);
}
