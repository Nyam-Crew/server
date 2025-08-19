package com.nyam.everyday.module.notification.repository;

import com.nyam.everyday.module.notification.entity.Notification;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  // 특정 유저에게 전달할 알림 목록을 가져온다. 10개만 부를 것
  @Query("SELECT n "
      + "FROM Notification n "
      + "WHERE n.member.memberId = :memberId OR n.member IS NULL "
      + "ORDER BY n.notificationId DESC")
  List<Notification> findNotificationByMemberId(@Param("memberId") Long memberId, Pageable pageable);

  @Query("SELECT COALESCE(MAX(n.notificationId), 0L) "
      + "FROM Notification n "
      + "WHERE n.member.memberId = :memberId OR n.member.memberId IS NULL")
  Long findMaxNotificationId(@Param("memberId") Long memberId);
}
