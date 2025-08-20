package com.nyam.everyday.module.team.repository;

import com.nyam.everyday.module.team.entity.TeamNotification;
import com.nyam.everyday.module.team.enums.DeliveryStatus;
import com.nyam.everyday.module.team.enums.TeamNotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 그룹 알림 관련 Repository
 *
 * @author : 이지은
 * @fileName : TeamNotificationRepository
 * @since : 25. 8. 11.
 */
public interface TeamNotificationRepository extends JpaRepository<TeamNotification, Long> {
    @Modifying
    @Query("delete from TeamNotification n where n.team.teamId = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);

    List<TeamNotification> findByDeliveryStatusAndNotificationTypeIn(DeliveryStatus deliveryStatus, List<TeamNotificationType> chat);
}
