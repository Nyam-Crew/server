package com.nyam.everyday.module.mission.repository;

import com.nyam.everyday.module.mission.entity.DailyMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyMissionRepository extends JpaRepository<DailyMission, Long> {
    int countByMemberIdAndMissionDateAndIsCompletedTrue(Long memberId, LocalDate missionDate);

    long countByMemberIdAndMissionDate(Long memberId, LocalDate missionDate);

    Optional<DailyMission> findByDailyMissionIdAndMemberId(Long id, Long memberId);

    @Query("select dm.mission.missionId from DailyMission dm where dm.memberId = :memberId and dm.missionDate = :missionDate")
    List<Long> findAssignedMissionIds(@Param("memberId") Long memberId, @Param("missionDate") LocalDate missionDate);

    // fetch join으로 mission을 함께 로딩 (Lazy 문제 해결)
    @Query("""
           select dm
             from DailyMission dm
             join fetch dm.mission m
            where dm.memberId = :memberId
              and dm.missionDate = :missionDate
            order by dm.dailyMissionId
           """)
    List<DailyMission> findWithMissionByMemberIdAndMissionDateOrderByDailyMissionIdAsc(
            @Param("memberId") Long memberId,
            @Param("missionDate") LocalDate missionDate
    );

    void deleteByMissionDateBefore(LocalDate cutoffDate);

    List<DailyMission> findByMemberIdAndMissionDate(Long memberId, LocalDate date);

}