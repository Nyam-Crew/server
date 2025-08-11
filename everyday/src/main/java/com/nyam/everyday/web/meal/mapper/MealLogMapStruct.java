package com.nyam.everyday.web.meal.mapper;

import com.nyam.everyday.module.meal.entity.MealLog;
import com.nyam.everyday.web.meal.dto.MealLogRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * MealLogMapStruct
 *
 * @author : 장소희
 * @fileName : MealLogMapStruct
 * @since : 25. 8. 5.
 */

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MealLogMapStruct {
    // DTO -> Entity (등록/수정용)
    MealLog toEntity(MealLogRequestDto dto);
}
