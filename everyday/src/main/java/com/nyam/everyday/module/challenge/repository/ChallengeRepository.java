package com.nyam.everyday.module.challenge.repository;

import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeCode;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.challenge.entity.ChallengeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

  Challenge getByChallengeCode(ChallengeCode challengeCode);

  List<Challenge> getAllByType(ChallengeType challengeType);

  List<Challenge> findAll();
}
