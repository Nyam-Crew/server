package com.nyam.everyday.module.team.repository;

import com.nyam.everyday.module.team.entity.TeamGlobalRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 팀 간 경쟁 관련 Repository
 *
 * @author : 이지은
 * @fileName : TeamGlobalRankingRepository
 * @since : 25. 8. 11.
 */
public interface TeamGlobalRankingRepository extends JpaRepository<TeamGlobalRanking, Long> {
    @Modifying
    @Query("delete from TeamGlobalRanking g where g.team.teamId = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);
}
