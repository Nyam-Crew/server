package com.nyam.everyday.module.mission.repository;

import com.nyam.everyday.module.mission.entity.DailyMissionStamp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyMissionStampRepository extends JpaRepository<DailyMissionStamp, Long> {
    Optional<DailyMissionStamp> findByMemberIdAndMissionDate(Long memberId, LocalDate date);
}