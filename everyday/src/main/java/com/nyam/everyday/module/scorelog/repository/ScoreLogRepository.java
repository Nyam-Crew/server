package com.nyam.everyday.module.scorelog.repository;

import com.nyam.everyday.module.meal.type.MealType;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.scorelog.entity.ScoreLog;
import com.nyam.everyday.module.scorelog.entity.SourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ScoreLogRepository extends JpaRepository<ScoreLog, Long> {

// --- ▼ [추가] createdDate를 기준으로 한 범위 조회 메소드를 새로 정의합니다 ---
    /** 오늘 하루 동안 특정 타입의 점수 기록이 있는지 확인 (출석, 물섭취 등) */
    boolean existsByMemberAndSourceTypeAndCreatedDateBetween(
            Member member,
            SourceType sourceType,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    /** 오늘 하루 동안 특정 식사 타입의 점수 기록이 있는지 확인 */
    boolean existsByMemberAndSourceTypeAndCreatedDateBetweenAndMealType(
            Member member,
            SourceType sourceType,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay,
            MealType mealType
    );

}
