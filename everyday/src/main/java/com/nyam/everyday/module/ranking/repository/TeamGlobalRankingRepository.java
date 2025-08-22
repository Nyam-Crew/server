package com.nyam.everyday.module.ranking.repository;

import com.nyam.everyday.module.ranking.entity.TeamGlobalRanking;
import java.util.Collection;
import java.util.List;
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


  @Query("""
    select r.team.teamId, r.rank
    from TeamGlobalRanking r
    where r.rankingYear = :year and r.rankingMonth = :month and r.team.teamId in :ids
  """)
  List<Object[]> findRanksByYearMonthAndTeamIds(@Param("year") int year,
      @Param("month") int month,
      @Param("ids") Collection<Long> teamIds);
}