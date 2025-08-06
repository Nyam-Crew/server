package com.nyam.everyday.module.challenge.repository;

import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeTitle;
import com.nyam.everyday.module.challenge.entity.ChallengeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

  List<Challenge> getAllByChallengeType(ChallengeType challengeType);

  Challenge getChallengeByChallengeTitle(ChallengeTitle title);
}
