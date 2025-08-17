package com.nyam.everyday.web.food.controller;

import com.nyam.everyday.search.food.dto.FoodSearchRequest;
import com.nyam.everyday.search.food.dto.FoodSearchResponse;
import com.nyam.everyday.search.food.dto.FoodSuggestionResponse;
import com.nyam.everyday.search.food.service.FoodSearchService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * FoodSearchController
 *
 * @author : 장소희
 * @fileName : FoodSearchController
 * @since : 25. 8. 15.
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/food/search")
@Validated
@Tag(name = "Food Search", description = "식품 검색 및 자동완성 API")
public class FoodSearchController {

    private static final int MAX_PAGE_SIZE = 50;

    private final FoodSearchService service;

    @GetMapping
    @Operation(
            summary = "식품 검색",
            description = "검색어(q) 하나로 식품명과 제조사를 동시에 검색합니다. 제조사 별 필터는 없습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "검색 성공")
            }
    )
    public FoodSearchResponse search(

            @Parameter(description = "검색어(식품명/제조사 동시 검색)", example = "샐러드")
            @RequestParam(required = false)
            String q,

            @Parameter(description = "페이지 번호(0-base)", example = "0", schema = @Schema(minimum = "0"))
            @RequestParam(defaultValue = "0") @Min(0)
            Integer page,

            @Parameter(description = "페이지 크기(최대 50)", example = "10", schema = @Schema(minimum = "1", maximum = "50"))
            @RequestParam(defaultValue = "10") @Min(1)
            Integer size,

            @Parameter(description = "정렬, 예) score,desc | unitKcal,asc", example = "score,desc")
            @RequestParam(defaultValue = "score,desc")
            String sort
    ) {
        q = normalize(q);
        size = Math.min(size, MAX_PAGE_SIZE);
        sort = sanitizeSort(sort);

        // manufacturer는 사용하지 않으므로 null로 고정
        var req = FoodSearchRequest.of(q, null, page, size, sort);
        return service.search(req);
    }

    @GetMapping("/suggest")
    @Operation(
            summary = "자동완성",
            description = "검색어 접두(prefix)에 맞는 식품명 후보를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "추천 성공")
            }
    )
    public FoodSuggestionResponse suggest(
            @Parameter(description = "자동완성 접두어", example = "샐")
            @RequestParam String prefix,

            @Parameter(description = "최대 개수(최대 50)", example = "10", schema = @Schema(minimum = "1", maximum = "50"))
            @RequestParam(defaultValue = "10") @Min(1)
            Integer size
    ) {
        prefix = normalize(prefix);
        size = Math.min(size, MAX_PAGE_SIZE);
        return service.suggest(prefix, size);
    }

    // ---- helpers ------------------------------------------------------------

    /** 공백 정리 + 빈 문자열이면 null 로 변환 */
    private String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** 허용된 필드만 통과시키는 간단한 정렬 파라미터 정제 */
    private String sanitizeSort(String sort) {
        if (sort == null || sort.isBlank()) return "score,desc";
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        String dir = (parts.length > 1 ? parts[1].trim().toLowerCase() : "desc");

        // 허용 필드 화이트리스트
        switch (field) {
            case "score":
            case "unitKcal":
            case "unitGram":
            case "foodSize":
                break;
            default:
                field = "score";
        }

        if (!dir.equals("asc") && !dir.equals("desc")) {
            dir = "desc";
        }
        return field + "," + dir;
    }
}