package com.nyam.everyday.search.food.mapper;

import com.nyam.everyday.module.food.entity.Food;
import com.nyam.everyday.search.food.document.FoodDocument;
import org.mapstruct.*;

/**
 * FoodSearchMapper
 *
 * @author : 장소희
 * @fileName : FoodSearchMapper
 * @since : 25. 8. 13.
 */

@Mapper(componentModel = "spring")
public interface FoodSearchMapper {

    @Mapping(target = "id",        expression = "java(food.getFoodId() == null ? null : String.valueOf(food.getFoodId()))")
    @Mapping(target = "foodName",  source = "foodName")
    @Mapping(target = "manufacturer", source = "manufacturer")
    @Mapping(target = "unitKcal",  expression = "java(food.getUnitKcal() == null ? null : food.getUnitKcal().doubleValue())")
    @Mapping(target = "unitGram", expression = "java(food.getUnitGram() == null ? null : Long.valueOf(food.getUnitGram()))")
    @Mapping(target = "foodSize",  source = "foodSize")
    FoodDocument from(Food food);
}