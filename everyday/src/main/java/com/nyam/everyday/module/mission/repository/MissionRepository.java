package com.nyam.everyday.module.mission.repository;

import com.nyam.everyday.module.mission.entity.Mission;
import com.nyam.everyday.module.mission.entity.MissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    @Query("select m from Mission m where m.isActive = true")
    List<Mission> findAllActive();

    List<Mission> findByTypeAndIsActiveTrue(MissionType type); // AUTO/ MANUAL 활성 미션 조회

}