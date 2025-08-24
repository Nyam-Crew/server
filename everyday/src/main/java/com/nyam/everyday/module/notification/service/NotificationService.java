package com.nyam.everyday.module.notification.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.notification.entity.MemberNotificationStatus;
import com.nyam.everyday.module.notification.entity.Notification;
import com.nyam.everyday.module.notification.entity.NotificationType;
import com.nyam.everyday.module.notification.repository.MemberNotificationStatusRepository;
import com.nyam.everyday.module.notification.repository.NotificationRepository;
import com.nyam.everyday.web.notification.dto.NotificationDto;
import com.nyam.everyday.web.notification.dto.NotificationStatusDto;
import com.nyam.everyday.web.notification.dto.NotifyToReactDto;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

  private final MemberRepository memberRepository;
  private final NotifyToReactService notifyToReactService;
  private final NotificationRepository notificationRepository;
  private final MemberNotificationStatusRepository memberNotificationStatusRepository;

  /// 특정 회원의 알림 목록을 최신순으로 20개 조회하고
  /// 해당 회원의 마지막 확인 알림 번호(lastNotificationNum)를 최신값으로 갱신합니다.
  // notification 리스트를 불러오고 해당 유저가 어디까지 알림을 확인했는지 갱신
  public List<NotificationDto> getNotifications(Long memberId) {

    // 멤버가 어디까지 알림을 읽었는지 불러오기
    MemberNotificationStatus memberNotificationStatus = memberNotificationStatusRepository.findByMember_MemberId(
        memberId).orElseThrow();
    Long lastNotifySeen = memberNotificationStatus.getLastNotificationNum();

    // 이 사람에게 전달할 알림 목록을 받아온다
    Pageable pageable = PageRequest.of(0, 20);
    List<Notification> notifyList = notificationRepository.findNotificationByMemberId(memberId, pageable);

    // 반환할 결과 Dto 배열 만들기
    List<NotificationDto> result = new ArrayList<>();
    for (Notification notification : notifyList) {
      result.add(NotificationDto.builder()
          .content(notification.getNotificationContent())
          .isRead(notification.getNotificationId() <= lastNotifySeen)
          .createdAt(notification.getCreatedDate())
          .build()
      );
    }

    // 지금 가져온 첫 원소의 NotificationId를 가져온다. 알림이 없으면 0으로 기본값 처리
    Long lastNotificationId = notifyList.isEmpty() ? 0 : notifyList.get(0).getNotificationId();

    // 값을 업데이트하고, 결과 반환
    memberNotificationStatus.setLastNotificationNum(lastNotificationId);
    return result;
  }

  /// 회원에게 새 알림이 존재하는지 확인하고 반환
  /// @Param 확인하고자 하는 사용자의 id
  /// @return 새 알림이 있으면 True, 없으면 False
  public NotificationStatusDto hasNewNotifications(Long memberId) {
    // 멤버 정보 가져오기
    Member member = memberRepository.findByMemberId(memberId).orElseThrow();

    // 현재 알람 테이블에서 가장 큰 키 값 조회
    Long maxNotifyNum = notificationRepository.findMaxNotificationId(memberId);

    // 유저가 어디까지 알람을 읽었는지 찾기 (없으면 새로 생성하고 0으로 지정)
    MemberNotificationStatus memberNotificationStatus = memberNotificationStatusRepository.findByMember_MemberId(memberId)
        .orElseGet(() -> memberNotificationStatusRepository.save(
            MemberNotificationStatus.builder()
                .member(member)
                .lastNotificationNum(0L)
                .build()
        ));


    // 키 값 기반으로 확인해서 새로 확인할 알림이 있다면 true, 아니면 false
    return new NotificationStatusDto(maxNotifyNum > memberNotificationStatus.getLastNotificationNum());
  }

  // 전체 공지 생성하고 /topic/notification 채널에 메세지 전송하기
  public void addBroadcastNotification(String content, NotificationType type) {
    Notification notification = Notification.builder()
        .member(null)
        .notificationContent(content)
        .notificationType(type)
        .build();

    // 생성된 공지 저장
    notification = notificationRepository.save(notification);

    // Dto로 변경
    NotifyToReactDto notifyToReactDto = NotifyToReactDto.builder()
        .content(notification.getNotificationContent())
        .createdAt(notification.getCreatedDate())
        .build();

    // 알림 전송
    notifyToReactService.NotifyBroadcast(notifyToReactDto);
  }

  // 개인 공지 생성하고 /user/queue/notification 채널에 메세지 전송하기
  public void addPrivateNotification(String content, Long memberId, NotificationType type) {
    Member member = memberRepository.findByMemberId(memberId)
        .orElseThrow(() -> BaseException.MEMBER_NOT_FOUND);

    Notification notification = Notification.builder()
        .member(member)
        .notificationContent(content)
        .notificationType(type)
        .build();

    // 생성된 공지 저장
    notification = notificationRepository.save(notification);

    // Dto로 변경
    NotifyToReactDto notifyToReactDto = NotifyToReactDto.builder()
        .content(notification.getNotificationContent())
        .createdAt(notification.getCreatedDate())
        .build();

    // 알림 전송
    notifyToReactService.NotifyToMember(notifyToReactDto, memberId);
  }

  /// 팀 공지 생성 시에 사용되어야 할 함수 지은님이 직접 구현해서 사용하시면 됩니다 (매개변수, 내용 등)
    /*팀알림 관련은 TeamNotificationService에서 처리합니다.*/
  public void addTeamNotification(String content, Long teamId, NotificationType type) {

    // 아래의 함수 사용하시면 Toast메세지가 해당 팀 멤버들에게 전달됩니다.
    /// notifyToReactService.notifyToTeam(NotifyToReactDto dto, Long TeamId)
  }
}
