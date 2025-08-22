package com.nyam.everyday.module.ranking.repository;

import com.nyam.everyday.module.ranking.entity.MemberGlobalRanking;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberGlobalRankingRepository extends JpaRepository<MemberGlobalRanking, Long> {

    @Query("""
    select r.member.memberId, r.rank
    from MemberGlobalRanking r
    where r.rankingYear = :year and r.rankingMonth = :month and r.member.memberId in :ids
  """)
    List<Object[]> findRanksByYearMonthAndMemberIds(@Param("year") int year,
        @Param("month") int month,
        @Param("ids") Collection<Long> memberIds);

    @Query("""
    select r.rank
    from MemberGlobalRanking r
    where r.rankingYear = :year and r.rankingMonth = :month and r.member.memberId = :memberId
  """)
    Optional<Integer> findRankByYearMonthAndMemberId(@Param("year") int year,
        @Param("month") int month,
        @Param("memberId") Long memberId);
}