package com.nyam.everyday.module.ranking.repository;

import com.nyam.everyday.module.ranking.entity.MemberGlobalRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberGlobalRankingRepository extends JpaRepository<MemberGlobalRanking, Long> {

}