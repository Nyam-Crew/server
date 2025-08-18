package com.nyam.everyday.module.summary.repository;

import com.nyam.everyday.module.summary.entity.MemberDailySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.time.LocalDate;

/**
 * MemberDailySummaryRepository
 *
 * @author : 장소희
 * @fileName : MemberDailySummaryRepository
 * @since : 25. 8. 7.
 */

@Repository
public interface MemberDailySummaryRepository extends JpaRepository<MemberDailySummary, Long> {

    // 월간 캘린더용: 요약 날짜(summary_date) 기준 범위 조회
    List<MemberDailySummary> findAllByMember_MemberIdAndSummaryDateBetweenOrderBySummaryDateAsc(
            Long memberId, LocalDate startDate, LocalDate endDate);

    Optional<MemberDailySummary> findByMember_MemberIdAndSummaryDate(Long memberId, LocalDate summaryDate);

}
