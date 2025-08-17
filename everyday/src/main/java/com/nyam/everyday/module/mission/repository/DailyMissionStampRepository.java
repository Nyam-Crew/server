package com.nyam.everyday.module.mission.repository;

import com.nyam.everyday.module.mission.entity.DailyMissionStamp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyMissionStampRepository extends JpaRepository<DailyMissionStamp, Long> {
    Optional<DailyMissionStamp> findByMemberIdAndMissionDate(Long memberId, LocalDate date);

    // ✅ 월간 캘린더용: 스탬프(도장) 범위 조회
    List<DailyMissionStamp> findByMemberIdAndMissionDateBetween(Long memberId, LocalDate start, LocalDate end);
}