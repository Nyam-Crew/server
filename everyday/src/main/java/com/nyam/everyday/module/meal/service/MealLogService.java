
package com.nyam.everyday.module.meal.service;

import com.nyam.everyday.web.meal.dto.MealLogRequestDto;
import com.nyam.everyday.web.meal.dto.MealLogResponseDto;
import com.nyam.everyday.module.meal.entity.MealLog;
import com.nyam.everyday.web.meal.mapper.MealLogMapStruct;
import com.nyam.everyday.module.meal.repository.MealLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    private final MealLogRepository mealLogRepository; // JPA Repository

    // 날짜별 기록 조회
    public List<MealLogResponseDto> getMealLogs(Long memberId, String mealType, String date) {
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime startOfDay = localDate.atStartOfDay();
        LocalDateTime endOfDay = localDate.plusDays(1).atStartOfDay().minusNanos(1);

        // mealType은 항상 필수로 들어옴!
        List<MealLog> logs = mealLogRepository.findByMemberIdAndMealTypeAndCreatedDateBetween(
                memberId, mealType, startOfDay, endOfDay
        );
        return mealLogMapStruct.toResponseDtoList(logs);
    }


    // 음식 기록 추가
    public void addMealLog(MealLogRequestDto requestDto) {
        MealLog mealLog = mealLogMapStruct.toEntity(requestDto);
        // createdDate, modifiedDate를 직접 세팅해야 한다면 여기서!
        mealLog.setCreatedDate(LocalDateTime.now());
        mealLog.setModifiedDate(LocalDateTime.now());
        mealLogRepository.save(mealLog);
    }

    // 필요에 따라 수정/삭제 메서드도 추가 가능
}
