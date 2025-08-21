package com.nyam.everyday.module.scorelog.service;

import com.nyam.everyday.module.meal.type.MealType;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.scorelog.entity.ScoreType;
import com.nyam.everyday.module.scorelog.entity.SourceType;
import com.nyam.everyday.module.scorelog.repository.ScoreLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 *
 *
 * @author : 이지은
 * @fileName : ScoreAwardService
 * @since : 25. 8. 19.
 *
 */
@Service
@RequiredArgsConstructor
public class ScoreAwardService{

    private final ScoreLogRepository scoreLogRepository;
    private final ScoreLogService scoreLogService;
    private final MealScoreWriter mealScoreWriter;


    // 출석: 하루 1회
    @Transactional
    public void awardAttendanceDailyOnce(Member member) {
        LocalDate today = LocalDate.now();
        if (scoreLogRepository.existsByMemberAndSourceTypeAndScoredOn(member, SourceType.ATTENDANCE, today)) return;
        scoreLogService.createScoreLog(member, (long) ScoreType.ATTENDANCE.defaultPoint(), SourceType.ATTENDANCE);
    }

    // 물기록: 하루 1회
    @Transactional
    public void awardWaterDailyOnce(Member member) {
        LocalDate today = LocalDate.now();
        if (scoreLogRepository.existsByMemberAndSourceTypeAndScoredOn(member, SourceType.WATER_INTAKE, today)) return;
        scoreLogService.createScoreLog(member, (long) ScoreType.WATER_INTAKE.defaultPoint(), SourceType.WATER_INTAKE);
    }

    // 체중: 최초 1회
    @Transactional
    public void awardWeightFirstTime(Member member) {
        if (scoreLogRepository.existsByMemberAndSourceType(member, SourceType.WEIGHT_LOG)) return;
        scoreLogService.createScoreLog(member, (long) ScoreType.WEIGHT_LOG.defaultPoint(), SourceType.WEIGHT_LOG);
    }

    // 식단: 슬롯별 하루 1회 (아/점/저/간)
    @Transactional
    public void awardMealSlotOnce(Member member, MealType slot) {
        LocalDate today = LocalDate.now();
        if (scoreLogRepository.existsByMemberAndSourceTypeAndScoredOnAndMealSlot(
                member, SourceType.MEAL_INPUT, today, slot)) return;

        // ⬇️ 여기서만 별도 Writer를 호출(기존 메서드 건드리지 않기 위해)
        mealScoreWriter.createMealSlotScoreLog(member, slot);
    }
}