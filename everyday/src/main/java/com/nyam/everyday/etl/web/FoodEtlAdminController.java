package com.nyam.everyday.etl.web;

import com.nyam.everyday.etl.service.FoodEtlService;
import com.nyam.everyday.etl.service.FoodEtlService.FullEtlReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * FoodEtlAdminController
 *
 * @author : 장소희
 * @fileName : FoodEtlAdminController
 * @since : 25. 8. 12.
 *
 * 관리자 전용 ETL 트리거 엔드포인트.
 * - 권한: ROLE_ADMIN
 * - 예: POST /api/admin/etl/foods/run?dryRun=true
 */

@Tag(name = "Food-Etl-Controller", description = "관리자 전용 Food Open Api ETL")
@RestController
@RequestMapping("/api/admin/etl/foods")
public class FoodEtlAdminController {

    private final FoodEtlService service;

    public FoodEtlAdminController(FoodEtlService service) {
        this.service = service;
    }

    @Operation(summary = "Food 전체 데이터 ETL 실행", description = "OpenAPI의 모든 페이지를 순회하여 식품/영양소 데이터를 DB와 Elasticsearch에 업서트합니다.")
    @PostMapping("/run")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FullEtlReport> run(
            @RequestParam(defaultValue = "false") boolean dryRun
    ) {
        FullEtlReport report = service.runFullEtl(dryRun);
        return ResponseEntity.ok(report);
    }
}
