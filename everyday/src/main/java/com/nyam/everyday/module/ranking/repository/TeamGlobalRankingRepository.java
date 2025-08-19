package com.nyam.everyday.module.ranking.repository;

import com.nyam.everyday.module.ranking.entity.TeamGlobalRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface TeamGlobalRankingRepository extends JpaRepository<TeamGlobalRanking, Long> {
  @Modifying
  @Query("DELETE FROM TeamGlobalRanking tgr WHERE tgr.team.teamId = :teamId")
  void deleteByTeamId(@Param("teamId") Long teamId);
}