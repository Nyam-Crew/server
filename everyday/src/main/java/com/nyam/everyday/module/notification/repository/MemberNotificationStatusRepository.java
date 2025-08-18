package com.nyam.everyday.module.notification.repository;

import com.nyam.everyday.module.notification.entity.MemberNotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberNotificationStatusRepository extends JpaRepository<MemberNotificationStatus, Long> {

}
