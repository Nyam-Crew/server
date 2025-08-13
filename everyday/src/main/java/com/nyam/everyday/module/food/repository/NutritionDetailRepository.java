package com.nyam.everyday.module.food.repository;

import com.nyam.everyday.module.food.entity.NutritionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * NutritionDetail Repository
 *
 * @author : 장소희
 * @fileName : NutritionDetailRepository
 * @since : 25. 8. 5.
 */

@Repository
public interface NutritionDetailRepository extends JpaRepository<NutritionDetail, Long> {
    Optional<NutritionDetail> findByFoodIdAndFoodCateIdAndNutritionNm(Long foodId, Long foodCateId, String nutritionNm);
}