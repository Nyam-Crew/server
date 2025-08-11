package com.nyam.everyday.module.team.repository;

import com.nyam.everyday.module.team.entity.TeamRankingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
/**
 * 팀 내 링킹 백업 Repository
 *
 * @author : 이지은
 * @fileName : TeamRankingHistoryRepository
 * @since : 25. 8. 11.
 */
public interface TeamRankingHistoryRepository extends JpaRepository<TeamRankingHistory, Long> {
    @Modifying
    @Query("delete from TeamRankingHistory r where r.team.teamId = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);
}
