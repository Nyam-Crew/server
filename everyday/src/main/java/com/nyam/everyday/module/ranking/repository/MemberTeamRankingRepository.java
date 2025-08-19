package com.nyam.everyday.module.ranking.repository;

import com.nyam.everyday.module.ranking.entity.MemberTeamRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberTeamRankingRepository extends JpaRepository<MemberTeamRanking, Long> {

}