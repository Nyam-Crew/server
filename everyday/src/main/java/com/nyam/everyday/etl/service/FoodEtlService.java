package com.nyam.everyday.etl.service;

import com.nyam.everyday.etl.api.NutriApiClient;
import com.nyam.everyday.etl.api.dto.NutriApiBody;
import com.nyam.everyday.etl.api.dto.NutriApiItem;
import com.nyam.everyday.etl.core.NutriConverters;
import com.nyam.everyday.etl.core.NutriConverters.Quantity;
import com.nyam.everyday.etl.core.NutriConverters.UnitKind;
import com.nyam.everyday.etl.core.NutrientType;
import com.nyam.everyday.module.food.entity.Food;
import com.nyam.everyday.module.food.entity.NutritionDetail;
import com.nyam.everyday.module.food.repository.FoodRepository;
import com.nyam.everyday.module.food.repository.NutritionDetailRepository;
import com.nyam.everyday.search.food.mapper.FoodSearchMapper;
import com.nyam.everyday.search.food.service.FoodSearchIndexer;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FoodEtlService {

    private static final int ROWS_PER_PAGE = 50; // OpenAPI 호출 시 페이지 당 행 수

    private final NutriApiClient api;
    private final FoodRepository foodRepo;
    private final NutritionDetailRepository ndRepo;
    private final FoodSearchIndexer foodIndexer;
    private final FoodSearchMapper foodMapper;
    private FoodEtlService self;

    public FoodEtlService(NutriApiClient api, FoodRepository foodRepo, NutritionDetailRepository ndRepo, FoodSearchIndexer foodIndexer, FoodSearchMapper foodMapper) {
        this.api = api;
        this.foodRepo = foodRepo;
        this.ndRepo = ndRepo;
        this.foodIndexer = foodIndexer;
        this.foodMapper = foodMapper;
    }

    @Lazy
    @Autowired
    public void setSelf(FoodEtlService self) {
        this.self = self;
    }

    // 전체 프로세스 총괄. 트랜잭션은 페이지 단위로 위임하므로 여기서는 제거.
    public FullEtlReport runFullEtl(boolean dryRun) {
        log.info("===== Food ETL Start (dryRun={}) =====", dryRun);
        FullEtlReport fullReport = new FullEtlReport();
        int currentPage = 1;
        long totalItems = 0;
        int totalPages = 1; // 최소 1번은 실행하기 위해 1로 시작

        do {
            NutriApiBody body;
            try {
                body = api.fetchItems(currentPage, ROWS_PER_PAGE);
            } catch (Exception e) {
                log.error("[ETL] API call failed for page {}. Stopping ETL.", currentPage, e);
                fullReport.addErrorSample("API call failed for page " + currentPage + ": " + e.getMessage());
                break; // API 호출 실패 시 중단
            }

            if (totalItems == 0) {
                if (StringUtils.hasText(body.getTotalCount())) {
                    totalItems = Long.parseLong(body.getTotalCount());
                    totalPages = (int) Math.ceil((double) totalItems / ROWS_PER_PAGE);
                    fullReport.setTotalItems(totalItems);
                    fullReport.setTotalPages(totalPages);
                }
            }

            List<NutriApiItem> items = body.getItems();
            log.info("[ETL] Page {}/{}: Received {} items.", currentPage, totalPages, items.size());

            // self-proxy를 통해 호출하여 트랜잭션 적용
            EtlReport pageReport = self.processPage(items, dryRun);
            fullReport.addPageReport(pageReport);

            if (items.isEmpty()) {
                log.info("[ETL] Reached last page (received 0 items).");
                break;
            }
            currentPage++;

        } while (currentPage <= totalPages);

        if (!dryRun) {
            try {
                log.info("[ETL] Flushing remaining documents to Elasticsearch...");
                foodIndexer.flush();
                log.info("[ETL] Elasticsearch flush completed.");
            } catch (Exception e) {
                log.error("[ETL] Final Elasticsearch flush failed.", e);
                fullReport.addErrorSample("[ES] Final flush failed: " + e.getMessage());
            }
        }

        log.info("===== Food ETL Finished =====");
        log.info("{}", fullReport);
        return fullReport;
    }

    public EtlReport processPage(List<NutriApiItem> items, boolean dryRun) {
        EtlReport r = new EtlReport(items.size());
        for (NutriApiItem it : items) {
            try {
                self.processSingleItem(it, dryRun, r);
            } catch (Exception e) {
                r.errors++;
                r.errorSamples.add(shortItem(it) + " :: " + e.getMessage());
                log.warn("[ETL] Failed to process item {}: {}", shortItem(it), e.getMessage(), e);
            }
        }
        return r;
    }

    @Transactional
    public void processSingleItem(NutriApiItem it, boolean dryRun, EtlReport r) {
        String name = safeTrim(it.getFoodNm());
        String mfr = emptyToNull(safeTrim(it.getMfrNm()));
        if (name == null) {
            r.skippedParse++;
            return;
        }

        Quantity base = NutriConverters.parseQuantity(it.getNutConSrtrQua())
                .orElse(new Quantity(new BigDecimal("100"), UnitKind.G));

        Food food = upsertFood(it, name, mfr, base, dryRun, r);

        if (!dryRun && food.getFoodId() != null) { // ID가 있어야만 다음 단계 진행
            try {
                foodIndexer.add(foodMapper.from(food));
            } catch (Exception e) {
                r.errors++;
                r.errorSamples.add("[ES] index add failed for foodId=" + food.getFoodId() + ": " + e.getMessage());
            }

            for (NutrientType nt : NutrientType.selected()) {
                BigDecimal raw = NutriConverters.parseNumber(getFieldByName(it, nt.apiField()))
                        .orElse(BigDecimal.ZERO);

                if (nt.unit() == NutrientType.Unit.MG) {
                    raw = NutriConverters.mgToG(raw);
                }

                BigDecimal per100g = NutriConverters.toPer100g(raw, base);
                per100g = NutriConverters.round1(NutriConverters.clamp4_1(per100g));

                upsertDetail(food.getFoodId(), nt.categoryId(), nt.label(), per100g, dryRun, r);
            }
        }
    }

    private Food upsertFood(NutriApiItem it, String name, String mfr,
                            Quantity base, boolean dryRun, EtlReport r) {

        BigDecimal kcal = NutriConverters.parseNumber(it.getEnerc()).orElse(BigDecimal.ZERO);
        BigDecimal kcalPer100g = NutriConverters.round1(
                NutriConverters.toPer100g(kcal, base)
        );

        Long unitGram = 100L;

        Integer foodSize = NutriConverters.parseQuantity(it.getFoodSize())
                .filter(q -> q.unit() == UnitKind.G)
                .map(q -> q.value().intValue())
                .orElse(null);

        Optional<Food> found =
                foodRepo.findByFoodNameAndManufacturerNullSafe(name, nvl(mfr, ""));

        if (found.isEmpty()) {
            Food f = Food.builder()
                    .foodName(name)
                    .manufacturer(mfr)
                    .unitKcal(kcalPer100g)
                    .unitGram(unitGram)
                    .foodSize(foodSize)
                    .build();

            if (!dryRun) return foodRepo.save(f);
            r.inserted++;
            return f;
        } else {
            Food existed = found.get();
            existed.setFoodName(name);
            existed.setManufacturer(mfr);
            existed.setUnitKcal(kcalPer100g);
            existed.setUnitGram(unitGram);
            existed.setFoodSize(foodSize);
            if (!dryRun) return foodRepo.save(existed);
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
                    .unitWeight(100L)
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
            case "prot" -> it.getProt();
            case "fatce" -> it.getFatce();
            case "chocdf" -> it.getChocdf();
            case "sugar" -> it.getSugar();
            case "fibtg" -> it.getFibtg();
            case "nat" -> it.getNat();
            case "chole" -> it.getChole();
            case "fasat" -> it.getFasat();
            case "fatrn" -> it.getFatrn();
            default -> null;
        };
    }

    private static String safeTrim(String s) {
        return (s == null) ? null : s.trim();
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private static String nvl(String s, String def) {
        return (s == null) ? def : s;
    }

    private static String shortItem(NutriApiItem it) {
        return "[" + it.getFoodCd() + "] " + it.getFoodNm();
    }

    public static class EtlReport {
        public final int received;
        public int inserted;
        public int updated;
        public int skippedParse;
        public int errors;
        public final List<String> errorSamples = new ArrayList<>();

        public EtlReport(int received) {
            this.received = received;
        }
    }

    @Getter
    public static class FullEtlReport {
        @Setter
        private long totalItems;
        @Setter
        private int totalPages;
        private int processedPages = 0;
        private long totalReceived = 0;
        private long totalInserted = 0;
        private long totalUpdated = 0;
        private long totalSkipped = 0;
        private long totalErrors = 0;
        private final List<String> errorSamples = new ArrayList<>();

        public void addPageReport(EtlReport report) {
            this.processedPages++;
            this.totalReceived += report.received;
            this.totalInserted += report.inserted;
            this.totalUpdated += report.updated;
            this.totalSkipped += report.skippedParse;
            this.totalErrors += report.errors;
            if (!report.errorSamples.isEmpty()) {
                this.errorSamples.addAll(report.errorSamples);
            }
        }
        
        public void addErrorSample(String error) {
            this.totalErrors++;
            this.errorSamples.add(error);
        }

        @Override
        public String toString() {
            return "FullEtlReport{" +
                    "totalItems=" + totalItems +
                    ", totalPages=" + totalPages +
                    ", processedPages=" + processedPages +
                    ", totalReceived=" + totalReceived +
                    ", totalInserted=" + totalInserted +
                    ", totalUpdated=" + totalUpdated +
                    ", totalSkipped=" + totalSkipped +
                    ", totalErrors=" + totalErrors +
                    '}';
        }
    }
}