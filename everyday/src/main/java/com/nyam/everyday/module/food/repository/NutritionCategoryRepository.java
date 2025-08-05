package com.nyam.everyday.module.food.repository;

import com.nyam.everyday.module.food.entity.NutritionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * NutritionCategory Repository
 *
 * @author : 장소희
 * @fileName : NutritionCategoryRepository
 * @since : 25. 8. 5.
 */

@Repository
public interface NutritionCategoryRepository extends JpaRepository<NutritionCategory, Long> {

}