package com.nyam.everyday.module.challenge.repository;

import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

  Challenge getByChallengeCode(ChallengeCode challengeCode);

  List<Challenge> getAllByType(ChallengeTag challengeTag);
}
