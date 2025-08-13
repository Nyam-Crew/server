package com.nyam.everyday.etl.service;

import com.nyam.everyday.etl.api.NutriApiClient;
import com.nyam.everyday.etl.api.dto.NutriApiItem;
import com.nyam.everyday.etl.core.NutrientType;
import com.nyam.everyday.etl.core.NutriConverters;
import com.nyam.everyday.etl.core.NutriConverters.Quantity;
import com.nyam.everyday.etl.core.NutriConverters.UnitKind;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.nyam.everyday.module.food.entity.Food;
import com.nyam.everyday.module.food.entity.NutritionDetail;
import com.nyam.everyday.module.food.repository.FoodRepository;
import com.nyam.everyday.module.food.repository.NutritionDetailRepository;
import com.nyam.everyday.search.service.FoodSearchIndexer;
import com.nyam.everyday.search.mapper.FoodSearchMapper;


/**
 * FoodEtlService
 *
 * @author : 장소희
 * @fileName : FoodEtlService
 * @since : 25. 8. 12.
 *
 * OpenAPI -> 표준화 -> 업서트 -> 리포트
 * - 항상 per 100g 저장, ml도 1:1 가정(라벨은 원본 보존하지 않아도 되면 스킵)
 */

@Service
public class FoodEtlService {

    private final NutriApiClient api;
    private final FoodRepository foodRepo;
    private final NutritionDetailRepository ndRepo;
    private final FoodSearchIndexer foodIndexer;
    private final FoodSearchMapper foodMapper;

    public FoodEtlService(NutriApiClient api, FoodRepository foodRepo, NutritionDetailRepository ndRepo, FoodSearchIndexer foodIndexer, FoodSearchMapper foodMapper) {
        this.api = api;
        this.foodRepo = foodRepo;
        this.ndRepo = ndRepo;
        this.foodIndexer = foodIndexer;
        this.foodMapper  = foodMapper;
    }

    @Transactional
    public EtlReport run(int page, int rows, boolean dryRun) {
        List<NutriApiItem> items = api.fetchItems(page, rows);

        EtlReport r = new EtlReport(page, rows, items.size());
        for (NutriApiItem it : items) {
            try {
                processOne(it, dryRun, r);
            } catch (Exception e) {
                r.errors++;
                r.errorSamples.add(shortItem(it) + " :: " + e.getMessage());
            }
        }
        // ES 벌크 전송 (dryRun이면 생략)
        if (!dryRun) {
            try {
                foodIndexer.flush();
            } catch (Exception e) {
                r.errors++;
                r.errorSamples.add("[ES] flush failed: " + e.getMessage());
            }
        }
        return r;
    }

    private void processOne(NutriApiItem it, boolean dryRun, EtlReport r) {
        String name = safeTrim(it.getFoodNm());
        String mfr  = emptyToNull(safeTrim(it.getMfrNm()));
        if (name == null) { r.skippedParse++; return; }

        // 기준량: "100g" / "100ml" -> Quantity (기본 100g)
        Quantity base = NutriConverters.parseQuantity(it.getNutConSrtrQua())
                .orElse(new Quantity(new BigDecimal("100"), UnitKind.G));

        // 1) food upsert
        Food food = upsertFood(it, name, mfr, base, dryRun, r);

        // ES 버퍼에 추가 (DB upsert가 성공했을 때만)
        if (!dryRun) {
            try {
                foodIndexer.add(foodMapper.from(food));
            } catch (Exception e) {
                // 인덱싱 실패는 리포트에만 기록하고 계속 진행
                r.errors++;
                r.errorSamples.add("[ES] index add failed for foodId=" + food.getFoodId() + ": " + e.getMessage());
            }
        }

        // 2) nutrition_detail upsert
        for (NutrientType nt : NutrientType.selected()) {
            BigDecimal raw = NutriConverters.parseNumber(getFieldByName(it, nt.apiField()))
                    .orElse(BigDecimal.ZERO);

            if (nt.unit() == NutrientType.Unit.MG) {
                raw = NutriConverters.mgToG(raw); // mg -> g
            }

            BigDecimal per100g = NutriConverters.toPer100g(raw, base);
            per100g = NutriConverters.round1(NutriConverters.clamp4_1(per100g));

            upsertDetail(food.getFoodId(), nt.categoryId(), nt.label(), per100g, dryRun, r);
        }
    }

    private Food upsertFood(NutriApiItem it, String name, String mfr,
                            Quantity base, boolean dryRun, EtlReport r) {

        // kcal per 100g 환산
        BigDecimal kcal = NutriConverters.parseNumber(it.getEnerc()).orElse(BigDecimal.ZERO);
        BigDecimal kcalPer100g = NutriConverters.round1(
                NutriConverters.toPer100g(kcal, base)
        );

        // 항상 100g 기준 저장
        Long unitGram = 100L;

        // foodSize: g만 허용(ml → g 환산 정책 미적용)
        Integer foodSize = NutriConverters.parseQuantity(it.getFoodSize())
                .filter(q -> q.unit() == UnitKind.G)
                .map(q -> q.value().intValue())
                .orElse(null);

        // (food_name, manufacturer)로 조회 (manufacturer NULL 가능 → 빈문자 대체)
        Optional<Food> found =
                foodRepo.findByFoodNameAndManufacturerNullSafe(name, nvl(mfr, ""));

        if (found.isEmpty()) {
            Food f = Food.builder()
                    .foodName(name)
                    .manufacturer(mfr)
                    .unitKcal(kcalPer100g)
                    .unitGram(unitGram)   // Long 필드
                    .foodSize(foodSize)     // Integer 필드
                    .build();

            if (!dryRun) foodRepo.save(f);
            r.inserted++;
            return f;
        } else {
            Food existed = found.get();
            existed.setFoodName(name);          // 혹시 원본명이 바뀐 경우 동기화(선택)
            existed.setManufacturer(mfr);       // 제조사도 동기화(선택)
            existed.setUnitKcal(kcalPer100g);
            existed.setUnitGram(unitGram);
            existed.setFoodSize(foodSize);
            if (!dryRun) foodRepo.save(existed);
            r.updated++;
            return existed;
        }
    }

    private void upsertDetail(Long foodId, int cateId, String nm, BigDecimal amount,
                              boolean dryRun, EtlReport r) {

        Optional<NutritionDetail> found =
                ndRepo.findByFoodIdAndFoodCateIdAndNutritionNm(foodId, (long) cateId, nm);

        if (found.isEmpty()) {
            NutritionDetail nd = NutritionDetail.builder()
                    .foodId(foodId)
                    .foodCateId((long) cateId)
                    .nutritionNm(nm)
                    .amount(amount)
                    .unitWeight(100L) // NutritionDetail이 BIGINT라면 Long 사용
                    .build();

            if (!dryRun) ndRepo.save(nd);
            r.inserted++;
        } else {
            NutritionDetail existed = found.get();
            existed.setAmount(amount);
            existed.setUnitWeight(100L);
            if (!dryRun) ndRepo.save(existed);
            r.updated++;
        }
    }

    private static String getFieldByName(NutriApiItem it, String apiField) {
        return switch (apiField) {
            case "prot"  -> it.getProt();
            case "fatce" -> it.getFatce();
            case "chocdf"-> it.getChocdf();
            case "sugar" -> it.getSugar();
            case "fibtg" -> it.getFibtg();
            case "nat"   -> it.getNat();
            case "chole" -> it.getChole();
            case "fasat" -> it.getFasat();
            case "fatrn" -> it.getFatrn();
            default -> null;
        };
    }

    private static String safeTrim(String s) { return (s == null) ? null : s.trim(); }
    private static String emptyToNull(String s) { return (s == null || s.isBlank()) ? null : s; }
    private static String nvl(String s, String def) { return (s == null) ? def : s; }

    /** 간단 리포트 */
    public static class EtlReport {
        public final int page;
        public final int rows;
        public final int received;
        public int inserted;
        public int updated;
        public int skippedParse;
        public int errors;
        public final List<String> errorSamples = new ArrayList<>();
        public EtlReport(int page, int rows, int received) {
            this.page = page; this.rows = rows; this.received = received;
        }
    }

    private static String shortItem(NutriApiItem it) {
        return "[" + it.getFoodCd() + "] " + it.getFoodNm();
    }
}