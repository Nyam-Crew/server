package com.nyam.everyday.etl.web;


import com.nyam.everyday.etl.service.FoodEtlService;
import com.nyam.everyday.etl.service.FoodEtlService.EtlReport;
import io.swagger.v3.oas.annotations.Operation;
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
 * - 예: POST /api/admin/etl/foods/run?page=1&rows=100&dryRun=true
 */

@RestController
@RequestMapping("/api/admin/etl/foods")
public class FoodEtlAdminController {

    private final FoodEtlService service;

    public FoodEtlAdminController(FoodEtlService service) {
        this.service = service;
    }

    @Operation(summary = "Food ETL 실행", description = "OpenAPI에서 식품/영양소를 조회하여 DB에 업서트합니다.")
    @PostMapping("/run")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EtlReport> run(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int rows,
            @RequestParam(defaultValue = "false") boolean dryRun
    ) {
        EtlReport report = service.run(page, rows, dryRun);
        return ResponseEntity.ok(report);
    }
}
