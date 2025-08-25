package com.nyam.everyday.module.challenge.repository;

import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.MemberChallengeDay;
import com.nyam.everyday.module.member.entity.Member;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberChallengeDayRepository extends JpaRepository<MemberChallengeDay, Long> {

  // 특정 유저의 특정 날짜 데이터가 있는지 확인하기
  Boolean existsByMember_MemberIdAndChallenge_IdAndTargetDate(Long member_memberId, Long challenge_id, LocalDate targetDate);

  // 특정 유저의 특정 날짜 데이터 가져오기
  @Query("SELECT mcd "
      + "FROM MemberChallengeDay mcd "
      + "WHERE mcd.member.memberId = :memberId "
      + "AND mcd.challenge.id = :challengeId "
      + "AND mcd.targetDate = :targetDate")
  MemberChallengeDay findMemberChallengeDay(@Param("memberId") Long memberId, @Param("challengeId") Long ChallengeId, @Param("targetDate") LocalDate targetDate);

  // 특정 유저의 특정 챌린지 관련 데이터 세기
  @Query("SELECT COUNT(mcd) "
      + "FROM MemberChallengeDay mcd "
      + "WHERE mcd.member.memberId = :memberId "
      + "AND mcd.challenge.id = :challengeId")
  Integer countMemberChallengeDay(@Param("memberId") Long memberId, @Param("challengeId") Long challengeId);

  // 특정 유저의 특정 챌린지 관련 특정 날짜의 데이터 삭제하기
  @Modifying
  @Query("DELETE FROM MemberChallengeDay mcd "
      + "WHERE mcd.member.memberId = :memberId "
      + "AND mcd.challenge.id = :challengeId "
      + "AND mcd.targetDate = :targetDate")
  void deleteMemberChallengeDay(@Param("memberId") Long memberId, @Param("challengeId") Long ChallengeId, @Param("targetDate") LocalDate targetDate);
}
