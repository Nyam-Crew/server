package com.nyam.everyday.module.food.repository;

import com.nyam.everyday.module.food.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Food Repository
 *
 * @author : 장소희
 * @fileName : FoodRepository
 * @since : 25. 8. 5.
 */

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

}