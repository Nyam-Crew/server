package com.nyam.everyday.etl.core;

import java.util.Arrays;
import java.util.List;

/**
 * NutrientType
 *
 * @author : 장소희
 * @fileName : NutrientType
 * @since : 25. 8. 12.
 * 선택 영양소 매핑(카테고리/라벨/원본 API 필드).
 *  - 카테고리 ID는 시드 기준: 탄수화물=1, 단백질=2, 지방=3, 무기질=4
 */

public enum NutrientType {
    PROTEIN("prot",  "단백질",    2, Unit.G),
    FAT("fatce",     "지방",      3, Unit.G),
    CARB("chocdf",   "탄수화물",  1, Unit.G),
    SUGAR("sugar",   "당류",      1, Unit.G),
    FIBER("fibtg",   "식이섬유",  1, Unit.G),
    SODIUM("nat",    "나트륨",    4, Unit.MG),
    CHOLE("chole",   "콜레스테롤",3, Unit.MG),
    FASAT("fasat",   "포화지방산",3, Unit.G),
    FATRN("fatrn",   "트랜스지방산",3, Unit.G);

    public enum Unit { G, MG }

    private final String apiField;
    private final String label;
    private final int categoryId;
    private final Unit unit;

    NutrientType(String apiField, String label, int categoryId, Unit unit) {
        this.apiField = apiField;
        this.label = label;
        this.categoryId = categoryId;
        this.unit = unit;
    }

    public String apiField() { return apiField; }
    public String label() { return label; }
    public int categoryId() { return categoryId; }
    public Unit unit() { return unit; }

    public static List<NutrientType> selected() {
        return Arrays.asList(values());
    }
}
