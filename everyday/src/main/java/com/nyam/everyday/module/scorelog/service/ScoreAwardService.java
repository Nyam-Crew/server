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
import java.time.LocalDateTime;
import java.time.LocalTime;

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
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        if (scoreLogRepository.existsByMemberAndSourceTypeAndCreatedDateBetween(
                member, SourceType.ATTENDANCE, startOfDay, endOfDay)) return;
        scoreLogService.createScoreLog(member, (long) ScoreType.ATTENDANCE.defaultPoint(), SourceType.ATTENDANCE);
    }

    // 물기록: 하루 1회
    @Transactional
    public void awardWaterDailyOnce(Member member) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        if (scoreLogRepository.existsByMemberAndSourceTypeAndCreatedDateBetween(
                member, SourceType.WATER_INTAKE, startOfDay, endOfDay)) return;
        scoreLogService.createScoreLog(member, (long) ScoreType.WATER_INTAKE.defaultPoint(), SourceType.WATER_INTAKE);
    }

    // 체중: 최초 1회 (변경 없음)
    @Transactional
    public void awardWeightDailyOnce(Member member) { // [이름 변경] awardWeightFirstTime -> awardWeightDailyOnce
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        // [로직 변경] 날짜와 무관한 조회 -> 날짜 범위 조회로 변경
        if (scoreLogRepository.existsByMemberAndSourceTypeAndCreatedDateBetween(
                member, SourceType.WEIGHT_LOG, startOfDay, endOfDay)) return;

        scoreLogService.createScoreLog(member, (long) ScoreType.WEIGHT_LOG.defaultPoint(), SourceType.WEIGHT_LOG);
    }

    // 식단: 슬롯별 하루 1회 (아/점/저/간)
    @Transactional
    public void awardMealSlotOnce(Member member, MealType slot) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        if (scoreLogRepository.existsByMemberAndSourceTypeAndCreatedDateBetweenAndMealType(
                member, SourceType.MEAL_INPUT, startOfDay, endOfDay, slot)) return;
        mealScoreWriter.createMealSlotScoreLog(member, slot);
    }
}