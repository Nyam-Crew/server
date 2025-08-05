package com.nyam.everyday.web.meal.mapper;

import com.nyam.everyday.module.meal.entity.MealLog;
import com.nyam.everyday.web.meal.dto.MealLogRequestDto;
import com.nyam.everyday.web.meal.dto.MealLogResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * MealLogMapStruct
 *
 * @author : 장소희
 * @fileName : MealLogMapStruct
 * @since : 25. 8. 5.
 */

@Mapper(componentModel = "spring")
public interface MealLogMapStruct {
    MealLogMapStruct INSTANCE = Mappers.getMapper(MealLogMapStruct.class);

    // DTO -> Entity (등록/수정용)
    MealLog toEntity(MealLogRequestDto dto);

    // Entity -> DTO (응답용)
    MealLogResponseDto toResponseDto(MealLog entity);

    // Entity 리스트 -> DTO 리스트
    List<MealLogResponseDto> toResponseDtoList(List<MealLog> entityList);
}
