package com.nyam.everyday.module.team.repository;

import com.nyam.everyday.module.team.entity.TeamNotification;
import com.nyam.everyday.module.team.enums.DeliveryStatus;
import com.nyam.everyday.module.team.enums.TeamNotificationType;
import org.springframework.data.domain.Pageable;
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

    // 멤버의 팀 알림 최신순 20개
    @Query("""
        select tn
        from TeamNotification tn
        where tn.member.memberId = :memberId
        order by tn.teamAlarmId desc
    """)
    List<TeamNotification> findLatestByMember(@Param("memberId") Long memberId, Pageable pageable);

    // 멤버의 '안 읽은' 팀 알림 존재 여부
    @Query("""
        select case when count(tn) > 0 then true else false end
        from TeamNotification tn
        where tn.member.memberId = :memberId
          and tn.isChecked = false
    """)
    boolean existsUnreadForMember(@Param("memberId") Long memberId);

    // 특정 멤버가 특정 팀의 특정 타입 알림을 모두 읽음 처리
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update TeamNotification tn
           set tn.isChecked = true
         where tn.member.memberId = :memberId
           and tn.team.teamId = :teamId
           and tn.notificationType = :type
           and tn.isChecked = false
    """)
    int markCheckedByMemberTeamAndType(@Param("memberId") Long memberId,
                                       @Param("teamId") Long teamId,
                                       @Param("type") TeamNotificationType type);

    // SUMMARY 읽음 처리(팀 단위)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update TeamNotification tn
           set tn.isChecked = true
         where tn.member.memberId = :memberId
           and tn.team.teamId    = :teamId
           and tn.notificationType = com.nyam.everyday.module.team.enums.TeamNotificationType.SUMMARY
           and tn.isChecked = false
    """)
    int markSummaryCheckedByMemberAndTeam(@Param("memberId") Long memberId,
                                          @Param("teamId") Long teamId);

    // 멤버의 모든 미읽음(타입 불문) 일괄 읽음 처리 (알림함 열었을 때)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update TeamNotification tn
           set tn.isChecked = true
         where tn.member.memberId = :memberId
           and tn.isChecked = false
    """)
    int markAllUncheckedForMember(@Param("memberId") Long memberId);

    // 단건 읽음 처리 (이미 읽었으면 0건)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update TeamNotification tn
           set tn.isChecked = true
         where tn.teamAlarmId = :notificationId
           and tn.member.memberId = :memberId
           and tn.isChecked = false
    """)
    int markOneChecked(@Param("memberId") Long memberId,
                       @Param("notificationId") Long notificationId);

}
