package com.nyam.everyday.module.meal.repository;

import com.nyam.everyday.module.meal.entity.MealLog;
import com.nyam.everyday.module.meal.type.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Date;
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
            "m.mealLogId, m.member.memberId, m.food.foodId, f.foodName, " +
            "m.intakeAmount, m.intakeKcal, m.mealType, m.createdDate, m.modifiedDate) " +
            "FROM MealLog m JOIN m.food f " +
            "WHERE m.member.memberId = :memberId AND m.mealType = :mealType AND m.mealLogDate = :date")
    List<MealLogResponseDto> findMealLogsWithFoodName(
            @Param("memberId") Long memberId,
            @Param("mealType") String mealType,
            @Param("date") Date date
    );

    interface LiteRow {
        Long getMealLogId();
        String getFoodName();
        java.math.BigDecimal getIntakeKcal();
        MealType getMealType();
    }

    @Query("""
      select m.mealLogId as mealLogId,
             f.foodName  as foodName,
             m.intakeKcal as intakeKcal,
             m.mealType  as mealType
      from MealLog m join m.food f
      where m.member.memberId = :memberId
        and m.mealLogDate = :date
      order by m.mealType, m.mealLogId
    """)
    List<LiteRow> findLiteByMemberAndDate(@Param("memberId") Long memberId,
                                          @Param("date") Date date);

    //특정 멤버의 특정 날짜, 특정 식사 타입에 해당하는 모든 기록을 생성일 오름차순으로 조회합니다. (피드 score 계산을 위해)
    List<MealLog> findByMember_MemberIdAndMealLogDateAndMealTypeOrderByCreatedDateAsc(Long memberId, Date date, MealType mealType);

}