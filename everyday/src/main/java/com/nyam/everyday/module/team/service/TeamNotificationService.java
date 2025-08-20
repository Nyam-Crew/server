package com.nyam.everyday.module.team.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.notification.service.NotifyToReactService;
import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.module.team.entity.TeamNotification;
import com.nyam.everyday.module.team.enums.DeliveryStatus;
import com.nyam.everyday.module.team.enums.TeamNotificationType;
import com.nyam.everyday.module.team.repository.TeamMemberStatusRepository;
import com.nyam.everyday.module.team.repository.TeamNotificationRepository;
import com.nyam.everyday.module.team.repository.TeamRepository;
import com.nyam.everyday.web.notification.dto.NotifyToReactDto;
import com.nyam.everyday.web.team.dto.TeamNotifyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
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

        List<Member> recipients = getApprovedTeamMembers(teamId).stream()
                .filter(member -> !member.getMemberId().equals(actorMemberId))
                .toList();

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
}
