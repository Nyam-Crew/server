
package com.nyam.everyday.module.meal.service;

import com.nyam.everyday.module.food.entity.Food;
import com.nyam.everyday.module.food.repository.FoodRepository;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
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
        // DTO -> Entity 변환
        MealLog mealLog = mealLogMapStruct.toEntity(mealLogRequestDto);

        // 영속 엔티티로 세팅하기 위해 조회
        Food food = foodRepository.findById(mealLogRequestDto.getFoodId())
                .orElseThrow(() -> new IllegalArgumentException("Food not found with id: " + mealLogRequestDto.getFoodId()));

        Member member = memberRepository.findById(mealLogRequestDto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + mealLogRequestDto.getMemberId()));

        mealLog.setFood(food);
        mealLog.setMember(member);

        // 서버에서 createdDate, modifiedDate가 없으면 현재시간으로 세팅
        LocalDateTime now = LocalDateTime.now();
        if (mealLog.getCreatedDate() == null) {
            mealLog.setCreatedDate(now);
        }
        if (mealLog.getModifiedDate() == null) {
            mealLog.setModifiedDate(now);
        }

        MealLog saved = mealLogRepository.save(mealLog);

        return saved.getMealLogId();
    }

    @Transactional
    public void updateIntakeAmountAndKcal(Long userId, Long mealLogId, Integer intakeAmount, Double intakeKcal){
        MealLog mealLog = mealLogRepository.findById(mealLogId)
                .orElseThrow(() -> new IllegalArgumentException("MealLog not found with id: " + mealLogId));

        if (!mealLog.getMember().getMemberId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        if (intakeAmount != null) {
            mealLog.setIntakeAmount(intakeAmount);
        }
        if (intakeKcal != null) {
            mealLog.setIntakeKcal(BigDecimal.valueOf(intakeKcal));
        }

        mealLog.setModifiedDate(LocalDateTime.now()); // 수정일시 업데이트

        mealLogRepository.save(mealLog);
    }

    public void deleteMealLog(Long mealLogId) {
        mealLogRepository.deleteById(mealLogId);
    }

}
