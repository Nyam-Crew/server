package com.nyam.everyday.module.notification.repository;

import com.nyam.everyday.module.notification.entity.MemberNotificationStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberNotificationStatusRepository extends JpaRepository<MemberNotificationStatus, Long> {

  Optional<MemberNotificationStatus> findByMemberId(Long memberId);
}
