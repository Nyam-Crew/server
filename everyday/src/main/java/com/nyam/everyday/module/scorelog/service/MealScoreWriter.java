package com.nyam.everyday.module.scorelog.service;

import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.ranking.service.RankingService;
import com.nyam.everyday.module.scorelog.entity.MealType;
import com.nyam.everyday.module.scorelog.entity.ScoreLog;
import com.nyam.everyday.module.scorelog.entity.ScoreType;
import com.nyam.everyday.module.scorelog.entity.SourceType;
import com.nyam.everyday.module.scorelog.repository.ScoreLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * 식단용 스코어 writer
 *
 * @author : 이지은
 * @fileName : MealScoreWriter
 * @since : 25. 8. 19.
 *
 */
@Component
@RequiredArgsConstructor
public class MealScoreWriter {

    private final ScoreLogRepository scoreLogRepository;
    private final RankingService rankingService;

    @Transactional
    public void createMealSlotScoreLog(Member member, MealType mealType) {
        ScoreLog log = ScoreLog.builder()
                .member(member)
                .scoreAmount((long) ScoreType.MEAL_LOG.defaultPoint())
                .sourceType(SourceType.MEAL_INPUT)
                .mealType(mealType)                 // ★ 슬롯 저장
                // scoredOn은 @PrePersist에서 LocalDate.now()로 셋업 권장
                .build();

        scoreLogRepository.save(log);
        rankingService.updateMemberScore(member.getMemberId(), (long) ScoreType.MEAL_LOG.defaultPoint());
    }
}