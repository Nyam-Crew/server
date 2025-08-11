
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * MealLogService
 *
 * @author : 장소희
 * @fileName : MealLogService
 * @since : 25. 8. 5.
 */

@Service
@RequiredArgsConstructor
public class MealLogService {

    private final MealLogMapStruct mealLogMapStruct;
    private final MealLogRepository mealLogRepository;
    private final FoodRepository foodRepository;
    private final MemberRepository memberRepository;
    private final MemberDailySummaryRepository memberDailySummaryRepository;

    // 날짜별 기록 조회
    public List<MealLogResponseDto> getMealLogs(Long memberId, String mealType, String date) {
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime startOfDay = localDate.atStartOfDay();
        LocalDateTime endOfDay = localDate.plusDays(1).atStartOfDay().minusNanos(1);

        return mealLogRepository.findMealLogsWithFoodName(
                memberId, mealType, startOfDay, endOfDay
        );
    }

    @Transactional
    public Long addMealLog(MealLogRequestDto mealLogRequestDto) {
        // DTO를 Entity로 변환
        MealLog mealLog = mealLogMapStruct.toEntity(mealLogRequestDto);

        // 식품과 회원 정보 조회 (영속성 관리용)
        Food food = foodRepository.findById(mealLogRequestDto.getFoodId())
                .orElseThrow(() -> new IllegalArgumentException("Food not found with id: " + mealLogRequestDto.getFoodId()));

        Member member = memberRepository.findById(mealLogRequestDto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + mealLogRequestDto.getMemberId()));

        mealLog.setFood(food);
        mealLog.setMember(member);

        LocalDateTime now = LocalDateTime.now();

        // 생성/수정 일시가 없으면 현재 시간으로 설정
        if (mealLog.getCreatedDate() == null) {
            mealLog.setCreatedDate(now);
        }
        if (mealLog.getModifiedDate() == null) {
            mealLog.setModifiedDate(now);
        }

        // 음식 기록 저장
        MealLog savedMealLog = mealLogRepository.save(mealLog);

        // 오늘 날짜 기준 일일 요약 데이터 범위
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusNanos(1);

        // 해당 회원의 오늘 일일 요약 데이터 조회, 없으면 새로 생성
        MemberDailySummary summary = memberDailySummaryRepository
                .findByMember_MemberIdAndCreatedDateBetween(member.getMemberId(), startOfDay, endOfDay)
                .orElseGet(() -> createNewSummary(member, now));

        // 요청 DTO에서 영양소 정보 추출 (null일 경우 0으로 초기화)
        BigDecimal protein = safeValue(mealLogRequestDto.getProtein());
        BigDecimal carbohydrate = safeValue(mealLogRequestDto.getCarbohydrate());
        BigDecimal fat = safeValue(mealLogRequestDto.getFat());
        BigDecimal kcal = safeValue(mealLogRequestDto.getIntakeKcal());

        // 총 칼로리가 null이면 0으로 초기화
        if (summary.getTotalKcal() == null) {
            summary.setTotalKcal(BigDecimal.ZERO);
        }



        // 기존 총 섭취량에 이번 기록의 영양소 누적
        summary.setTotalProtein(addIntegerSafe(summary.getTotalProtein(), protein.intValue()));
        summary.setTotalCarbohydrate(addIntegerSafe(summary.getTotalCarbohydrate(), carbohydrate.intValue()));
        summary.setTotalFat(addIntegerSafe(summary.getTotalFat(), fat.intValue()));

        // 총 칼로리 누적 업데이트
        summary.setTotalKcal(summary.getTotalKcal().add(kcal));

        summary.setModifiedDate(now);

        // 일일 요약 저장
        memberDailySummaryRepository.save(summary);

        return savedMealLog.getMealLogId();
    }

    /**
     * 섭취량 및 칼로리 일부 수정 + 일일 요약 동시 업데이트
     */
    @Transactional
    public void updateIntakeAmountAndKcal(Long userId, Long mealLogId,
                                          Integer intakeAmount, Double intakeKcal,
                                          BigDecimal protein, BigDecimal carbohydrate, BigDecimal fat) {
        // 1) 권한/기존 로그 조회
        MealLog mealLog = mealLogRepository.findById(mealLogId)
                .orElseThrow(() -> new IllegalArgumentException("MealLog not found with id: " + mealLogId));

        if (!mealLog.getMember().getMemberId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        // 2) 이전 총량(=로그에 저장된 총량) 안전하게 확보
        BigDecimal oldProteinTotal = safeValue(mealLog.getProtein());        // 예: 300.0  (총 g)
        BigDecimal oldCarbTotal    = safeValue(mealLog.getCarbohydrate());   // 예: 400.0
        BigDecimal oldFatTotal     = safeValue(mealLog.getFat());            // 예: 500.0
        BigDecimal oldKcalTotal    = safeValue(mealLog.getIntakeKcal());     // 예: 200.0
        int oldIntakeAmount = mealLog.getIntakeAmount() == null ? 0 : mealLog.getIntakeAmount();

        // 3) 새 총량 만들기 (파라미터가 null이면 기존값 유지)
        Integer    newIntakeAmount = intakeAmount != null ? intakeAmount : oldIntakeAmount;
        BigDecimal newProteinTotal = protein       != null ? protein       : oldProteinTotal;
        BigDecimal newCarbTotal    = carbohydrate  != null ? carbohydrate  : oldCarbTotal;
        BigDecimal newFatTotal     = fat           != null ? fat           : oldFatTotal;
        BigDecimal newKcalTotal    = intakeKcal    != null ? BigDecimal.valueOf(intakeKcal) : oldKcalTotal;

        // 4) delta 계산 (총량 기준, ×섭취량 금지!)
        BigDecimal proteinDelta = newProteinTotal.subtract(oldProteinTotal);
        BigDecimal carbDelta    = newCarbTotal.subtract(oldCarbTotal);
        BigDecimal fatDelta     = newFatTotal.subtract(oldFatTotal);
        BigDecimal kcalDelta    = newKcalTotal.subtract(oldKcalTotal);

        // 5) 요약 대상 날짜는 "그 로그의 날짜"로 산정
        LocalDate logDate = mealLog.getCreatedDate().toLocalDate();
        LocalDateTime startOfDay = logDate.atStartOfDay();
        LocalDateTime endOfDay   = logDate.plusDays(1).atStartOfDay().minusNanos(1);

        MemberDailySummary summary = memberDailySummaryRepository
                .findByMember_MemberIdAndCreatedDateBetween(userId, startOfDay, endOfDay)
                .orElseThrow(() -> new IllegalStateException("MemberDailySummary not found for user and date"));

        // 6) 일일 합계 갱신 (delta 반영)
        //    단백질/탄수/지방이 INT 컬럼이면 int로 변환하기 전에 범위 체크 + 음수 방지
        int newTotalProtein = safeIntAdd(summary.getTotalProtein(), proteinDelta);
        int newTotalCarb    = safeIntAdd(summary.getTotalCarbohydrate(), carbDelta);
        int newTotalFat     = safeIntAdd(summary.getTotalFat(), fatDelta);

        BigDecimal newTotalKcal = safeScale(summary.getTotalKcal().add(kcalDelta)); // numeric(?,1) 가정

        summary.setTotalProtein(Math.max(0, newTotalProtein));
        summary.setTotalCarbohydrate(Math.max(0, newTotalCarb));
        summary.setTotalFat(Math.max(0, newTotalFat));
        if (newTotalKcal.compareTo(BigDecimal.ZERO) < 0) newTotalKcal = BigDecimal.ZERO;
        summary.setTotalKcal(newTotalKcal);
        summary.setModifiedDate(LocalDateTime.now());

        // 7) 로그 자체 업데이트 (총량을 그대로 저장)
        mealLog.setIntakeAmount(newIntakeAmount);
        mealLog.setProtein(newProteinTotal);
        mealLog.setCarbohydrate(newCarbTotal);
        mealLog.setFat(newFatTotal);
        mealLog.setIntakeKcal(newKcalTotal);
        mealLog.setModifiedDate(LocalDateTime.now());

        // 8) 저장
        mealLogRepository.save(mealLog);
        memberDailySummaryRepository.save(summary);
    }

    /**
     * 식사 기록 삭제
     */
    @Transactional
    public void deleteMealLog(Long userId, Long mealLogId) {
        // 1) 기존 로그 조회 + 권한 확인
        MealLog mealLog = mealLogRepository.findById(mealLogId)
                .orElseThrow(() -> new IllegalArgumentException("MealLog not found with id: " + mealLogId));

        if (!mealLog.getMember().getMemberId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        // 2) 기존 총량(=이 로그 1건의 총량) 확보
        BigDecimal oldProteinTotal = safeValue(mealLog.getProtein());
        BigDecimal oldCarbTotal    = safeValue(mealLog.getCarbohydrate());
        BigDecimal oldFatTotal     = safeValue(mealLog.getFat());
        BigDecimal oldKcalTotal    = safeValue(mealLog.getIntakeKcal());

        // 3) summary 찾기 (로그 날짜 기준)
        LocalDate logDate = mealLog.getCreatedDate().toLocalDate();
        LocalDateTime startOfDay = logDate.atStartOfDay();
        LocalDateTime endOfDay   = logDate.plusDays(1).atStartOfDay().minusNanos(1);

        MemberDailySummary summary = memberDailySummaryRepository
                .findByMember_MemberIdAndCreatedDateBetween(userId, startOfDay, endOfDay)
                .orElseThrow(() -> new IllegalStateException("MemberDailySummary not found for user and date"));

        // 4) delta = -기존 총량
        int newTotalProtein = safeIntAdd(summary.getTotalProtein(), oldProteinTotal.negate());
        int newTotalCarb    = safeIntAdd(summary.getTotalCarbohydrate(), oldCarbTotal.negate());
        int newTotalFat     = safeIntAdd(summary.getTotalFat(), oldFatTotal.negate());

        BigDecimal newTotalKcal = safeScale(summary.getTotalKcal().subtract(oldKcalTotal));

        // 5) 음수 방지
        summary.setTotalProtein(Math.max(0, newTotalProtein));
        summary.setTotalCarbohydrate(Math.max(0, newTotalCarb));
        summary.setTotalFat(Math.max(0, newTotalFat));
        if (newTotalKcal.compareTo(BigDecimal.ZERO) < 0) newTotalKcal = BigDecimal.ZERO;
        summary.setTotalKcal(newTotalKcal);
        summary.setModifiedDate(LocalDateTime.now());

        // 6) 저장 후 로그 삭제
        memberDailySummaryRepository.save(summary);
        mealLogRepository.delete(mealLog);
    }


    /* ======= 유틸 메서드 ======= */

    /**
     * null 체크 후 BigDecimal 반환, null일 경우 0 반환
     */
    private BigDecimal safeValue(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    /**
     * null 안전하게 Integer 덧셈 수행, null은 0으로 처리
     */
    private Integer addIntegerSafe(Integer a, Integer b) {
        int first = a != null ? a : 0;
        return Math.addExact(first, b);
    }

    /**
     * 새로운 일일 요약 데이터 생성 헬퍼
     */
    private MemberDailySummary createNewSummary(Member member, LocalDateTime now) {
        return MemberDailySummary.builder()
                .member(member)
                .weight(member.getWeight())
                .totalProtein(0)
                .totalCarbohydrate(0)
                .totalFat(0)
                .totalWater(0)
                .totalKcal(BigDecimal.ZERO)
                .createdDate(now)
                .modifiedDate(now)
                .build();
    }

    /** 합계(INT) + delta(BigDecimal)를 안전하게 더해 int로 변환 */
    private static int safeIntAdd(int base, BigDecimal delta) {
        // 소수점은 반올림(필요 시 내림/올림 정책 결정)
        BigDecimal result = BigDecimal.valueOf(base).add(delta);
        // int 범위 보호 (원하면 명시적 상한/하한도 두세요)
        if (result.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
            throw new ArithmeticException("총량이 int 범위를 초과했습니다.");
        }
        if (result.compareTo(BigDecimal.valueOf(Integer.MIN_VALUE)) < 0) {
            // 아래에서 0으로 클램프하니 보통 여기까지는 안 올 것
            return 0;
        }
        return result.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    /** numeric(?, scale) 컬럼용 스케일 고정 */
    private static BigDecimal safeScale(BigDecimal v) {
        return v.setScale(1, RoundingMode.HALF_UP);
    }
}