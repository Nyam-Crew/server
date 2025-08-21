package com.nyam.everyday.module.summary.repository;

import com.nyam.everyday.module.summary.entity.MemberDailySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;
import java.util.List;

/**
 * MemberDailySummaryRepository
 *
 * @author : 장소희
 * @fileName : MemberDailySummaryRepository
 * @since : 25. 8. 7.
 */

@Repository
public interface MemberDailySummaryRepository extends JpaRepository<MemberDailySummary, Long> {

    Optional<MemberDailySummary> findByMember_MemberIdAndSummaryDate(Long memberId, Date summaryDate);

    List<MemberDailySummary> findAllByMember_MemberIdAndSummaryDateGreaterThanEqualAndSummaryDateLessThanOrderBySummaryDateAsc(Long memberId, Date fromInclusive, Date toExclusive);
}


