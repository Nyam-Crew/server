package com.nyam.everyday.module.meal.service;

import com.nyam.everyday.module.food.entity.Food;
import com.nyam.everyday.module.food.repository.FoodRepository;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.summary.entity.MemberDailySummary;
import com.nyam.everyday.module.summary.repository.MemberDailySummaryRepository;
import com.nyam.everyday.web.meal.dto.MealLogRequestDto;
import com.nyam.everyday.web.meal.dto.MealLogResponseDto;
import com.nyam.everyday.module.meal.entity.MealLog;
import com.nyam.everyday.web.meal.mapper.MealLogMapStruct;
import com.nyam.everyday.module.meal.repository.MealLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealLogService {

    private final MealLogMapStruct mealLogMapStruct;
    private final MealLogRepository mealLogRepository;
    private final FoodRepository foodRepository;
    private final MemberRepository memberRepository;
    private final MemberDailySummaryRepository memberDailySummaryRepository;

    /* =========================
       날짜별 기록 조회
       ========================= */
    @Transactional(readOnly = true)
    public List<MealLogResponseDto> getMealLogs(Long memberId, String mealType, String date) {
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime startOfDay = localDate.atStartOfDay();
        LocalDateTime endOfDay = localDate.plusDays(1).atStartOfDay().minusNanos(1);
        return mealLogRepository.findMealLogsWithFoodName(memberId, mealType, startOfDay, endOfDay);
    }

    /* =========================
       식사 기록 추가 + 일일요약 누적
       ========================= */
    @Transactional
    public Long addMealLog(MealLogRequestDto dto) {
        // 1) DTO -> Entity
        MealLog mealLog = mealLogMapStruct.toEntity(dto);

        // 2) 연관 엔티티 로드
        Food food = foodRepository.findById(dto.getFoodId())
                .orElseThrow(() -> new IllegalArgumentException("Food not found. id=" + dto.getFoodId()));
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found. id=" + dto.getMemberId()));

        mealLog.setFood(food);
        mealLog.setMember(member);

        LocalDateTime now = LocalDateTime.now();
        if (mealLog.getCreatedDate() == null) mealLog.setCreatedDate(now);
        if (mealLog.getModifiedDate() == null) mealLog.setModifiedDate(now);

        // 3) 로그 저장
        MealLog saved = mealLogRepository.save(mealLog);

        // 4) 요약 로우 upsert(요약일자는 "오늘")
        LocalDate summaryDate = LocalDate.now();
        MemberDailySummary summary = memberDailySummaryRepository
                .findByMember_MemberIdAndSummaryDate(member.getMemberId(), summaryDate)
                .orElseGet(() -> createNewSummary(member, summaryDate, now));

        // 5) DTO 내 total(=합계) 값을 그대로 누적
        BigDecimal protein      = nz(dto.getProtein());       // g
        BigDecimal carbohydrate = nz(dto.getCarbohydrate());  // g
        BigDecimal fat          = nz(dto.getFat());           // g
        BigDecimal kcalBD       = nz(dto.getIntakeKcal());    // kcal (소수 입력 가능)

        summary.setTotalProtein     ( nz(summary.getTotalProtein()).add(protein) );
        summary.setTotalCarbohydrate( nz(summary.getTotalCarbohydrate()).add(carbohydrate) );
        summary.setTotalFat         ( nz(summary.getTotalFat()).add(fat) );
        // 물 섭취는 식사 로그에 없다면 0 유지 (별도 API에서 증가)
        // 칼로리만 Integer 누적
        int addKcal = kcalBD.setScale(0, RoundingMode.HALF_UP).intValue();
        summary.setTotalKcal( safeInt(summary.getTotalKcal()) + addKcal );

        summary.setModifiedDate(now);
        memberDailySummaryRepository.save(summary);

        return saved.getMealLogId();
    }

    /* =========================
       일부 섭취 총량/칼로리 수정 + 요약 delta 반영
       ========================= */
    @Transactional
    public void updateIntakeAmountAndKcal(Long userId, Long mealLogId,
                                          Integer intakeAmount, Double intakeKcal,
                                          BigDecimal protein, BigDecimal carbohydrate, BigDecimal fat) {
        // 1) 권한/기존 로그
        MealLog log = mealLogRepository.findById(mealLogId)
                .orElseThrow(() -> new IllegalArgumentException("MealLog not found. id=" + mealLogId));
        if (!log.getMember().getMemberId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        // 2) 기존 총량
        BigDecimal oldProtein = nz(log.getProtein());
        BigDecimal oldCarb    = nz(log.getCarbohydrate());
        BigDecimal oldFat     = nz(log.getFat());
        BigDecimal oldKcal    = nz(log.getIntakeKcal());

        // 3) 새 총량(파라미터 null이면 기존값 유지)
        Integer    newIntakeAmount = intakeAmount != null ? intakeAmount : (log.getIntakeAmount() == null ? 0 : log.getIntakeAmount());
        BigDecimal newProtein      = protein      != null ? protein      : oldProtein;
        BigDecimal newCarb         = carbohydrate != null ? carbohydrate : oldCarb;
        BigDecimal newFat          = fat          != null ? fat          : oldFat;
        BigDecimal newKcal         = intakeKcal   != null ? BigDecimal.valueOf(intakeKcal) : oldKcal;

        // 4) delta = 새 - 옛
        BigDecimal dProtein = newProtein.subtract(oldProtein);
        BigDecimal dCarb    = newCarb.subtract(oldCarb);
        BigDecimal dFat     = newFat.subtract(oldFat);
        BigDecimal dKcal    = newKcal.subtract(oldKcal);

        // 5) 요약 로우(해당 로그 날짜)
        LocalDate logDate = log.getCreatedDate().toLocalDate();
        MemberDailySummary summary = memberDailySummaryRepository
                .findByMember_MemberIdAndSummaryDate(userId, logDate)
                .orElseThrow(() -> new IllegalStateException("MemberDailySummary not found for user/date"));

        // 6) 요약 누적(=기존 + delta)
        summary.setTotalProtein     ( nz(summary.getTotalProtein()).add(dProtein) );
        summary.setTotalCarbohydrate( nz(summary.getTotalCarbohydrate()).add(dCarb) );
        summary.setTotalFat         ( nz(summary.getTotalFat()).add(dFat) );

        int kcalDelta = dKcal.setScale(0, RoundingMode.HALF_UP).intValue();
        int newTotalKcal = safeInt(summary.getTotalKcal()) + kcalDelta;
        summary.setTotalKcal(Math.max(0, newTotalKcal));
        summary.setModifiedDate(LocalDateTime.now());

        // 7) 로그 자체 갱신(총량 저장)
        log.setIntakeAmount(newIntakeAmount);
        log.setProtein(newProtein);
        log.setCarbohydrate(newCarb);
        log.setFat(newFat);
        log.setIntakeKcal(newKcal);
        log.setModifiedDate(LocalDateTime.now());

        mealLogRepository.save(log);
        memberDailySummaryRepository.save(summary);
    }

    /* =========================
       식사 기록 삭제 + 요약 차감
       ========================= */
    @Transactional
    public void deleteMealLog(Long userId, Long mealLogId) {
        MealLog log = mealLogRepository.findById(mealLogId)
                .orElseThrow(() -> new IllegalArgumentException("MealLog not found. id=" + mealLogId));
        if (!log.getMember().getMemberId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        BigDecimal oldProtein = nz(log.getProtein());
        BigDecimal oldCarb    = nz(log.getCarbohydrate());
        BigDecimal oldFat     = nz(log.getFat());
        BigDecimal oldKcal    = nz(log.getIntakeKcal());

        LocalDate logDate = log.getCreatedDate().toLocalDate();
        MemberDailySummary summary = memberDailySummaryRepository
                .findByMember_MemberIdAndSummaryDate(userId, logDate)
                .orElseThrow(() -> new IllegalStateException("MemberDailySummary not found for user/date"));

        // 누적값에서 해당 로그 총량 차감
        summary.setTotalProtein     ( clampNz(summary.getTotalProtein().subtract(oldProtein)) );
        summary.setTotalCarbohydrate( clampNz(summary.getTotalCarbohydrate().subtract(oldCarb)) );
        summary.setTotalFat         ( clampNz(summary.getTotalFat().subtract(oldFat)) );

        int minusKcal = oldKcal.setScale(0, RoundingMode.HALF_UP).intValue();
        summary.setTotalKcal(Math.max(0, safeInt(summary.getTotalKcal()) - minusKcal));
        summary.setModifiedDate(LocalDateTime.now());

        memberDailySummaryRepository.save(summary);
        mealLogRepository.delete(log);
    }

    /* =========================
       헬퍼들
       ========================= */
    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
    private static BigDecimal clampNz(BigDecimal v) {
        if (v == null) return BigDecimal.ZERO;
        return v.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : v;
    }
    private static int safeInt(Integer v) {
        return v == null ? 0 : v;
    }

    private MemberDailySummary createNewSummary(Member member, LocalDate summaryDate, LocalDateTime now) {
        return MemberDailySummary.builder()
                .member(member)
                .summaryDate(summaryDate)
                .weight(member.getWeight())                    // 없으면 null 가능
                .totalProtein(BigDecimal.ZERO)                 // g
                .totalCarbohydrate(BigDecimal.ZERO)            // g
                .totalFat(BigDecimal.ZERO)                     // g
                .totalWater(BigDecimal.ZERO)                   // ml (별도 API에서 업데이트)
                .totalKcal(0)                                  // kcal (정수 누적)
                .createdDate(now)
                .modifiedDate(now)
                .build();
    }
}