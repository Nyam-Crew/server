package com.nyam.everyday.module.meal.repository;

import com.nyam.everyday.module.meal.entity.MealLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
/**
 * MealLogRepository
 *
 * @author : 장소희
 * @fileName : MealLogRepository
 * @since : 25. 8. 5.
 */

public interface MealLogRepository extends JpaRepository<MealLog, Long> {
    List<MealLog> findByMemberIdAndMealTypeAndCreatedDateBetween(
            Long memberId,
            String mealType,
            LocalDateTime start,
            LocalDateTime end
    );

}