package com.nyam.everyday.module.team.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.chatting.chatroom.registry.ChatRoomRegistry;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.notification.repository.MemberNotificationStatusRepository;
import com.nyam.everyday.module.notification.service.NotifyToReactService;
import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.module.team.entity.TeamNotification;
import com.nyam.everyday.module.team.enums.DeliveryStatus;
import com.nyam.everyday.module.team.enums.TeamNotificationType;
import com.nyam.everyday.module.team.repository.TeamMemberStatusRepository;
import com.nyam.everyday.module.team.repository.TeamNotificationRepository;
import com.nyam.everyday.module.team.repository.TeamRepository;
import com.nyam.everyday.web.notification.dto.NotificationStatusDto;
import com.nyam.everyday.web.team.dto.TeamNotificationBoxDto;
import com.nyam.everyday.web.team.dto.TeamNotifyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * 팀 알림 서비스
 *
 * @author : 이지은
 * @fileName : TeamNotificationService
 * @since : 25. 8. 20.
 *
 */
@Service
@RequiredArgsConstructor
public class TeamNotificationService {

    private final TeamNotificationRepository teamNotificationRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberStatusRepository teamMemberStatusRepository;

    private final NotifyToReactService notifyToReactService;
    private final ChatRoomRegistry chatRoomRegistry;


    // ✅ 팀 알림 전용 RedisTemplate 주입
    @Qualifier("redisTeamNotificationTemplate")
    private final RedisTemplate<String, String> redisTemplate;

    // 타입별 쿨다운 시간 설정
    private Duration cooldownOf(TeamNotificationType type) {
        return switch (type) {
            case CHAT -> Duration.ofSeconds(30);
            case FEED -> Duration.ofMinutes(3);
            default   -> Duration.ZERO; // NOTICE
        };
    }

    private String cooldownKey(TeamNotificationType type, Long teamId) {
        return "team:noty:cooldown:%s:team:%d".formatted(type.name(), teamId);
    }

    @Transactional
    public void addTeamNotification(Long actorMemberId, Long teamId, TeamNotificationType type, String content) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BaseException(ErrorCode.GROUP_NOT_FOUND));

        // 승인된 전체 팀원 조회
        List<Member> allApprovedMembers = getApprovedTeamMembers(teamId);
        List<Member> recipients; // 최종 알림 수신자 리스트

        // << [2] 알림 타입에 따라 수신자 필터링 로직 분기
        if (type == TeamNotificationType.CHAT) {
            // 현재 채팅방을 구독 중인 멤버 ID 리스트를 가져옵니다.
            List<Long> presentMemberIdsList = chatRoomRegistry.findSubscribers(teamId);

            // 효율적인 비교를 위해 접속 중인 멤버의 ID를 Set으로 변환합니다.
            java.util.Set<Long> presentMemberIds = new java.util.HashSet<>(presentMemberIdsList);

            // 전체 팀원에서 발신자와 현재 채팅방 접속자를 모두 제외합니다.
            recipients = allApprovedMembers.stream()
                    .filter(member -> !member.getMemberId().equals(actorMemberId)) // 발신자 제외
                    .filter(member -> !presentMemberIds.contains(member.getMemberId())) // 채팅방 접속자 제외
                    .toList();
        } else {
            // CHAT 타입이 아닐 경우, 기존 로직과 동일하게 발신자만 제외합니다.
            recipients = allApprovedMembers.stream()
                    .filter(member -> !member.getMemberId().equals(actorMemberId))
                    .toList();
        }

        if (recipients.isEmpty()) {
            return;
        }

        boolean immediate = false;
        if (cooldownOf(type).isZero()) {
            immediate = true;
        } else {
            Boolean first = redisTemplate.opsForValue()
                    .setIfAbsent(cooldownKey(type, teamId), "1", cooldownOf(type));
            immediate = Boolean.TRUE.equals(first);
        }

        // 1. 모든 알림을 우선 PENDING 상태로 생성
        List<TeamNotification> notifications = recipients.stream()
                .map(recipient -> TeamNotification.builder()
                        .team(team)
                        .member(recipient)
                        .notificationType(type)
                        .teamNotyContent(content)
                        .deliveryStatus(DeliveryStatus.PENDING) // 기본 상태 PENDING으로 설정
                        .build())
                .collect(Collectors.toList());

        // 2. 즉시 발송 조건일 경우, 도메인 메소드를 호출하여 상태 변경
        if (immediate) {
            notifications.forEach(TeamNotification::markAsImmediate);
        }


        teamNotificationRepository.saveAll(notifications);

        if (immediate) {
            TeamNotifyDto dto = TeamNotifyDto.builder()
                    .content("[" + team.getTeamTitle() + "] " + content)
                    .createdAt(LocalDateTime.now())
                    .notificationId(notifications.get(0).getTeamAlarmId())
                    .type(type)
                    .build();
            notifyToReactService.NotifyToTeam(dto, teamId);
        }
    }

    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void sendAggregatedNotifications() {
        List<TeamNotification> pendings = teamNotificationRepository
                .findByDeliveryStatusAndNotificationTypeIn(
                        DeliveryStatus.PENDING,
                        List.of(TeamNotificationType.CHAT, TeamNotificationType.FEED)
                );
        if (pendings.isEmpty()) return;

        Map<Team, Map<Member, List<TeamNotification>>> notificationsByUserAndTeam = pendings.stream()
                .collect(Collectors.groupingBy(
                        TeamNotification::getTeam,
                        Collectors.groupingBy(TeamNotification::getMember)
                ));

        List<TeamNotification> summaryNotifications = new ArrayList<>();

        notificationsByUserAndTeam.forEach((team, userNotificationsMap) -> {
            String teamPushSummary = "[%s] 새로운 소식이 있습니다.".formatted(team.getTeamTitle());
            TeamNotifyDto dto = TeamNotifyDto.builder()
                    .content(teamPushSummary)
                    .createdAt(LocalDateTime.now())
                    .type(TeamNotificationType.SUMMARY)
                    .build();
            notifyToReactService.NotifyToTeam(dto, team.getTeamId());

            userNotificationsMap.forEach((member, notificationList) -> {
                if (notificationList.isEmpty()) return;

                int count = notificationList.size();
                String userSpecificSummary = "[%s] %d개의 새로운 소식이 있습니다."
                        .formatted(team.getTeamTitle(), count);


                // 1. 요약 알림 엔티티 생성
                TeamNotification summaryNoty = TeamNotification.builder()
                        .team(team)
                        .member(member)
                        .notificationType(TeamNotificationType.SUMMARY)
                        .teamNotyContent(userSpecificSummary)
                        .isChecked(false)
                        .build();

                // 2. 도메인 메소드를 사용하여 상태를 BATCHED로 설정
                summaryNoty.markAsBatched();
                summaryNotifications.add(summaryNoty);

                // 3. 기존 PENDING 알림들의 상태를 도메인 메소드를 통해 PROCESSED로 변경
                notificationList.forEach(TeamNotification::markAsProcessed);

            });
        });

        if (!summaryNotifications.isEmpty()) {
            teamNotificationRepository.saveAll(summaryNotifications);
        }
        if (!pendings.isEmpty()) {
            teamNotificationRepository.saveAll(pendings);
        }
    }

    //팀에서 status approved인 멤버만 추출
    public List<Member> getApprovedTeamMembers(Long teamId) {
        List<TeamMemberStatus> approvedStatuses = teamMemberStatusRepository.findApprovedMembers(teamId);
        // TeamMemberStatus 리스트에서 Member 객체만 추출
        return approvedStatuses.stream()
                .map(TeamMemberStatus::getMember)
                .collect(Collectors.toList());
    }

    //=====================================================//

    // 1) 최신 알림 조회
    @Transactional(readOnly = true)
    public List<TeamNotificationBoxDto> getTeamNotifications(Long memberId) {
        Pageable pageable = PageRequest.of(0, 20);
        List<TeamNotification> rows = teamNotificationRepository.findLatestByMember(memberId, pageable);

        return rows.stream()
                .map(n -> TeamNotificationBoxDto.builder()
                        .content(n.getTeamNotyContent())
                        .createdAt(n.getCreatedDate())
                        .isRead(Boolean.TRUE.equals(n.getIsChecked()))
                        .build())
                .toList();
    }

    // 2) 새 알림이 있는지 판별 (안 읽은 isChecked=false가 1개라도 있으면 true)
    @Transactional(readOnly = true)
    public NotificationStatusDto hasNewTeamNotifications(Long memberId) {
        boolean hasNew = teamNotificationRepository.existsUnreadForMember(memberId);
        return new NotificationStatusDto(hasNew);
    }

    /**
     * 채팅/피드/공지 페이지 진입 시:
     * 해당 팀의 해당 타입 알림 + SUMMARY를 함께 읽음 처리
     */
    @Transactional
    public int markPageOpened(Long memberId, Long teamId, TeamNotificationType type) {
        int n1 = teamNotificationRepository.markCheckedByMemberTeamAndType(memberId, teamId, type);
        int n2 = teamNotificationRepository.markSummaryCheckedByMemberAndTeam(memberId, teamId); // ← 요게 핵심
        return n1 + n2;
    }

    /**
     * 알림함 열었을 때: 멤버의 모든 미읽음(요약 포함) 일괄 읽음
     */
    @Transactional
    public int markTeamInboxOpened(Long memberId) {
        return teamNotificationRepository.markAllUncheckedForMember(memberId);
    }

    //추후 정책 변경 대비 단건 읽음처리
    @Transactional
    public void markOneTeamNotificationChecked(Long memberId, Long notificationId) {
        teamNotificationRepository.markOneChecked(memberId, notificationId);
        // idempotent 쿼리(이미 읽은 건 0건 업데이트)라 부작용 없음
    }

    public ChatRoomRegistry getChatRoomRegistry() {
        return chatRoomRegistry;
    }
}
