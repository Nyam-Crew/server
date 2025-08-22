package com.nyam.everyday.module.ranking.repository;

import com.nyam.everyday.module.ranking.entity.MemberTeamRanking;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberTeamRankingRepository extends JpaRepository<MemberTeamRanking, Long> {

    @Query("""
    select r.member.memberId, r.rankInTeam
    from MemberTeamRanking r
    where r.rankingYear = :year and r.rankingWeek = :week and r.team.teamId = :teamId
      and r.member.memberId in :ids
  """)
    List<Object[]> findRanksByYearWeekTeamAndMemberIds(@Param("year") int year,
        @Param("week") int week,
        @Param("teamId") Long teamId,
        @Param("ids") Collection<Long> memberIds);
}
