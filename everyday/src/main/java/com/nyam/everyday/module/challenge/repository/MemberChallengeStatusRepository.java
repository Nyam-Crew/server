package com.nyam.everyday.module.challenge.repository;

import com.nyam.everyday.module.challenge.entity.MemberChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberChallengeStatusRepository extends JpaRepository<MemberChallengeStatus, Long> {
  Boolean existsByMember_MemberIdAndChallenge_ChallengeId(Long memberId, Long challengeId);

}
