package com.nyam.everyday.module.challenge.repository;

import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.MemberChallengeStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberChallengeStatusRepository extends JpaRepository<MemberChallengeStatus, Long> {

  @Query("SELECT mcs "
      + "FROM ")
  Optional<MemberChallengeStatus> getMCSByMemberChallenge(Long memberId, Long challengeId);

  @Query("SELECT mcs.isCleared "
      + "FROM MemberChallengeStatus mcs "
      + "WHERE mcs.member.memberId = :memberId AND mcs.challenge.id = :challengeId")
  Optional<Boolean> getIsCleared(Long memberId, Long challengeId);
}
