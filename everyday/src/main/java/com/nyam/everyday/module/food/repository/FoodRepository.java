package com.nyam.everyday.module.food.repository;

import com.nyam.everyday.module.food.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Food Repository
 *
 * @author : 장소희
 * @fileName : FoodRepository
 * @since : 25. 8. 5.
 */

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    @Query("""
        select f
          from Food f
         where lower(trim(f.foodName)) = lower(trim(:foodName))
           and (
                (f.manufacturer is null and :manufacturer = '')
                or lower(trim(f.manufacturer)) = lower(trim(:manufacturer))
           )
    """)
    Optional<Food> findByFoodNameAndManufacturerNullSafe(
            @Param("foodName") String foodName,
            @Param("manufacturer") String manufacturer
    );
}