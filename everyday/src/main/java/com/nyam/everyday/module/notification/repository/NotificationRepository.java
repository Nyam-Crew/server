package com.nyam.everyday.module.notification.repository;

import com.nyam.everyday.module.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
