package com.nyam.everyday.module.summary.repository;

import com.nyam.everyday.module.summary.entity.MemberDailySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
/**
 * MemberDailySummaryRepository
 *
 * @author : 장소희
 * @fileName : MemberDailySummaryRepository
 * @since : 25. 8. 7.
 */

@Repository
public interface MemberDailySummaryRepository extends JpaRepository<MemberDailySummary, Long> {

    // 회원별, 날짜별 조회를 위해 추가 가능 (예: 특정 날짜에 해당하는 summary 조회)
    Optional<MemberDailySummary> findByMember_MemberIdAndCreatedDateBetween(Long memberId, LocalDateTime start, LocalDateTime end);
}
