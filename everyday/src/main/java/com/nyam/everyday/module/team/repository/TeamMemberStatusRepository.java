package com.nyam.everyday.module.team.repository;

import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.module.team.enums.ParticipationStatus;
import com.nyam.everyday.module.team.enums.TeamRole;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 그룹 멤버 현황 관련 Repository
 *
 * @author : 이지은
 * @fileName : TeamMemberStatusRepository
 * @since : 25. 8. 6.
 */
@Repository
public interface TeamMemberStatusRepository extends JpaRepository<TeamMemberStatus, Long> {

    @Modifying
    @Query("delete from TeamMemberStatus s where s.team.teamId = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);

    Optional<TeamMemberStatus> findByTeam_TeamIdAndMember_MemberId(Long teamId, Long memberId);

    // [추가] 팀 ID와 역할(Role)로 멤버 상태 정보를 찾는 메소드
    List<TeamMemberStatus> findAllByTeam_TeamIdAndTeamRole(Long teamId, TeamRole teamRole);

    List<TeamMemberStatus> findAllByTeam_TeamIdAndStatus(Long teamId, ParticipationStatus participationStatus);

    @Query("SELECT tms FROM TeamMemberStatus tms JOIN FETCH tms.member WHERE tms.team.teamId = :teamId AND tms.status = :status")
    List<TeamMemberStatus> findAllWithMemberByTeam_TeamIdAndStatus(@org.springframework.data.repository.query.Param("teamId") Long teamId, @Param("status") ParticipationStatus status);

    @Query("""
        SELECT tms FROM TeamMemberStatus tms
        JOIN FETCH tms.member
        WHERE tms.team.teamId = :teamId
        AND tms.status = 'APPROVED'
    """)
    List<TeamMemberStatus> findApprovedMembers(@Param("teamId") Long teamId);

    // APPROVED 인원 카운트 (current_member_count 재검증/보정용)
    @Query("""
        SELECT COUNT(tms)
          FROM TeamMemberStatus tms
         WHERE tms.team.teamId = :teamId
           AND tms.status = 'APPROVED'
    """)
    long countJoinedMembers(@Param("teamId") Long teamId);

    Boolean existsByTeam_TeamIdAndMember_MemberIdAndStatus(Long teamId, Long memberId, ParticipationStatus status);

    // 특정 멤버가 속해있는 모든 그룹 정보 리스트 찾기
    List<TeamMemberStatus> getAllByMember_MemberId(Long memberId);

    //특정 멤버가 속해있는 모든 그룹 아이디 Set 찾기
    @Query("SELECT tms.team.teamId FROM TeamMemberStatus tms WHERE tms.member.memberId = :memberId AND tms.status = 'APPROVED'")
    Set<Long> findActiveTeamIdsByMemberId(@Param("memberId") Long memberId);

    //팀의 승인멤버 목록 조회 -> 팀 알림 메서드 호출할때 사용
    @Query("""
        select tms.member.memberId
        from TeamMemberStatus tms
        where tms.team.teamId = :teamId
          and tms.status = com.nyam.everyday.module.team.enums.ParticipationStatus.APPROVED
    """)
    List<Long> findApprovedMemberIdsByTeamId(Long teamId);
}
