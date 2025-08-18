package com.nyam.everyday.search.food.mapper;

import com.nyam.everyday.module.food.entity.Food;
import com.nyam.everyday.search.food.document.FoodDocument;
import com.nyam.everyday.search.food.dto.FoodSearchItem;
import com.nyam.everyday.search.food.dto.FoodSearchResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * FoodSearchMapper
 *
 * @author : 장소희
 * @fileName : FoodSearchMapper
 * @since : 25. 8. 13.
 */

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE // 불필요 매핑 경고 억제(선택)
)
public interface FoodSearchMapper {

    // --- 검색 응답: Document -> DTO ---
    FoodSearchItem toItem(FoodDocument doc);

    default FoodSearchResponse toPageResponse(Page<FoodDocument> page) {
        List<FoodSearchItem> items = page.getContent().stream()
                .map(this::toItem)
                .toList();
        return new FoodSearchResponse(
                items,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }

    // --- 인덱싱: Entity -> Document ---
    @Mapping(target = "id",
            expression = "java(food.getFoodId() == null ? null : String.valueOf(food.getFoodId()))")
    @Mapping(target = "foodName", source = "foodName")
    @Mapping(target = "manufacturer", source = "manufacturer")
    // BigDecimal -> Double null-safe 변환 (FoodDocument.unitKcal가 Double라고 가정)
    @Mapping(target = "unitKcal",
            expression = "java(food.getUnitKcal() == null ? null : food.getUnitKcal().doubleValue())")
    @Mapping(target = "unitGram", source = "unitGram") // Long -> Long
    @Mapping(target = "foodSize", source = "foodSize") // Integer -> Integer
    // ❗ 현재 매핑/Document에 all_search 없으면 아래 라인 제거
    // @Mapping(target = "all_search", ignore = true)
    FoodDocument from(Food food);
}