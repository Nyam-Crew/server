package com.nyam.everyday.module.scorelog.service;

import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.scorelog.entity.ScoreLog;
import com.nyam.everyday.module.scorelog.entity.PointType;
import com.nyam.everyday.module.scorelog.repository.ScoreLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ScoreLogService {

    private final ScoreLogRepository scoreLogRepository;
    private final RankingService rankingService;

    public void createScoreLog(Member member,  PointType pointType) {
        ScoreLog scoreLog = ScoreLog.builder()
                .member(member)
                .scoreAmount(scoreAmount)
                .pointType(pointType)
                .build();

        scoreLogRepository.save(scoreLog);
        rankingService.updateMemberScore(member.getMemberId(), scoreAmount);
    }
}
