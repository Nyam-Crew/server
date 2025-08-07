package com.nyam.everyday.module.meal.repository;

import com.nyam.everyday.module.meal.entity.MealLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import com.nyam.everyday.web.meal.dto.MealLogResponseDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * MealLogRepository
 *
 * @author : 장소희
 * @fileName : MealLog Repository
 * @since : 25. 8. 5.
 */

public interface MealLogRepository extends JpaRepository<MealLog, Long> {
    @Query("SELECT new com.nyam.everyday.web.meal.dto.MealLogResponseDto(" +
            "m.mealLogId, m.memberId, m.foodId, f.foodName, m.intakeAmount, m.intakeKcal, m.mealType, m.createdDate, m.modifiedDate) " +
            "FROM MealLog m JOIN Food f ON m.foodId = f.foodId " +
            "WHERE m.memberId = :memberId AND m.mealType = :mealType AND m.createdDate BETWEEN :start AND :end")
    List<MealLogResponseDto> findMealLogsWithFoodName(
            @Param("memberId") Long memberId,
            @Param("mealType") String mealType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}