package com.nyam.everyday.module.scorelog.repository;

import com.nyam.everyday.module.meal.type.MealType;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.scorelog.entity.ScoreLog;
import com.nyam.everyday.module.scorelog.entity.ScoreType;
import com.nyam.everyday.module.scorelog.entity.SourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ScoreLogRepository extends JpaRepository<ScoreLog, Long> {

    boolean existsByMemberAndSourceTypeAndScoredOn(Member m, SourceType s, LocalDate d);

    // 체중 : 최초 1회
    boolean existsByMemberAndSourceType(Member m, SourceType s);

    // 식단 : 슬롯별 하루 1회
    boolean existsByMemberAndSourceTypeAndScoredOnAndMealSlot(
            Member m, SourceType s, LocalDate d, MealType slot
    );
}
