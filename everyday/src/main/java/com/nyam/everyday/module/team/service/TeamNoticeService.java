package com.nyam.everyday.module.team.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.module.team.entity.TeamNotice;
import com.nyam.everyday.module.team.enums.*;
import com.nyam.everyday.module.team.repository.TeamMemberStatusRepository;
import com.nyam.everyday.module.team.repository.TeamNoticeRepository;
import com.nyam.everyday.module.team.repository.TeamRepository;
import com.nyam.everyday.module.team.util.FeedIds;
import com.nyam.everyday.web.team.dto.TeamActivityFeedItem;
import com.nyam.everyday.web.team.dto.TeamNoticeCreatedDto;
import com.nyam.everyday.web.team.dto.TeamNoticeDto;
import com.nyam.everyday.web.team.dto.TeamNoticeUpdateDto;
import com.nyam.everyday.web.team.mapper.TeamNoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

/**
 *그룹 공지 관련 서비스
 *
 * @author : 이지은
 * @fileName : TeamNoticeService
 * @since : 25. 8. 12.
 *
 */
//Todo. db에 인덱스 적용하여 DB 부하 줄이기
@Service
@RequiredArgsConstructor
public class TeamNoticeService {

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberStatusRepository teamMemberStatusRepository;
    private final TeamNoticeRepository teamNoticeRepository;
    private final TeamNoticeMapper teamNoticeMapper;

    private final TeamActivityFeedService feedService;
    private final TeamNotificationService teamNotificationService;

    @Transactional
    public TeamNoticeDto createNotice(Long teamId, Long actorMemberId, TeamNoticeCreatedDto req) {
        Team team = requireTeam(teamId);
        verifyEditor(teamId, actorMemberId);

        TeamNoticeType noticeType = req.getTeamNoticeType();
        if (noticeType == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST, "공지 타입을 지정해야 합니다.");
        }

        // "타입별 1건 제한" 로직
        if (teamNoticeRepository.existsByTeam_TeamIdAndTeamNoticeType(teamId, noticeType)) {
            throw new BaseException(ErrorCode.NOTICE_ALREADY_EXISTS,
                    "이미 해당 타입의 공지가 등록되어 있습니다. 기존 공지를 수정하거나 삭제한 뒤 다시 시도해주세요.");
        }

        Member writer = memberRepository.findById(actorMemberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        TeamNotice saved = teamNoticeRepository.save(
                TeamNotice.builder()
                        .team(team)
                        .member(writer)
                        .title(req.getTitle())
                        .content(req.getContent())
                        .teamNoticeType(noticeType)
                        .build()
        );

        // ✅ 마지막에 공지사항 피드 발행
        publishNoticeFeed(saved);

        // 공지 생성 후 팀 알림 생성 로직 호출
        teamNotificationService.addTeamNotification(
                actorMemberId,
                teamId,
                TeamNotificationType.NOTICE,
                "새로운 공지가 등록되었습니다."
        );

        return teamNoticeMapper.toNoticeDTO(saved);
    }

    @Transactional
    public TeamNoticeDto updateNotice(Long teamId, Long noticeId, Long actorMemberId, TeamNoticeUpdateDto dto) {
        requireTeam(teamId);
        verifyEditor(teamId, actorMemberId);

        TeamNotice notice = requireNoticeInTeam(teamId, noticeId);

        try {
            notice.editPartial(dto.getTitle(), dto.getContent());
        } catch (IllegalArgumentException e) {
            throw new BaseException(ErrorCode.INVALID_REQUEST, e.getMessage());
        }

        // ✅ 수정 후에도 피드를 다시 발행하여 내용 갱신 (수정됨 꼬리표 등)
        publishNoticeFeed(notice);

        // 공지 수정 알림 생성
        teamNotificationService.addTeamNotification(
                actorMemberId,
                teamId,
                TeamNotificationType.NOTICE,
                "공지가 수정되었습니다."
        );

        return teamNoticeMapper.toNoticeDTO(notice);
    }

    @Transactional(readOnly = true)
    public List<TeamNoticeDto> getNoticesByTeam(Long teamId, Long actorMemberId) {
        // 1. 공지를 볼 수 있는 권한이 있는지 확인하는 것은 동일합니다.
        verifyApproved(teamId, actorMemberId);

        // 2. 해당 팀의 '모든' 공지를 리스트로 가져옵니다. (Repository에 findAllByTeam_TeamId 추가 필요)
        List<TeamNotice> notices = teamNoticeRepository.findAllByTeam_TeamId(teamId);

        // 3. Mapper를 사용하여 List<TeamNotice>를 List<TeamNoticeDto>로 변환하여 반환합니다.
        return teamNoticeMapper.toNoticeDtoList(notices);
    }

    @Transactional
    public void deleteNotice(Long teamId, Long noticeId, Long actorMemberId) {
        verifyEditor(teamId, actorMemberId);

        TeamNotice notice = teamNoticeRepository
                .findByTeam_TeamIdAndTeamNoticeId(teamId, noticeId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOTICE_NOT_FOUND));


        // ✅ DB에서 삭제하기 전에, 관련 피드를 먼저 삭제
        removeNoticeFeed(teamId, noticeId);

        // 하드 삭제
        teamNoticeRepository.delete(notice);
        // 첨부/댓글 등 연관 데이터는 FK ON DELETE CASCADE 권장
    }

    // =================================================================
    // ✅ [신규] 공지사항 피드 발행/수정 헬퍼 메서드
    // =================================================================
    private void publishNoticeFeed(TeamNotice notice) {
        Long teamId = notice.getTeam().getTeamId();
        Set<Long> teamIds = Set.of(teamId); // 공지는 해당 팀에만 적용

        String feedId = FeedIds.notice(teamId, notice.getTeamNoticeId());

        // 피드의 생성 시간(ZSET의 score)은 최초 생성 시각으로 고정
        long createdAtMs = notice.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        TeamActivityFeedItem feedItem = TeamActivityFeedItem.builder()
                .feedId(feedId)
                .teamId(teamId) // 공지는 팀 전체 활동
                .activityType(ActivityType.NOTICE)
                .build();

        // 공지 피드는 다른 피드보다 길게 유지 (예: 7일)
        feedService.addFeedItemToTeams(teamIds, feedId, createdAtMs, feedItem, Duration.ofDays(7));
    }

    // =================================================================
    // ✅ [신규] 공지사항 피드 삭제 헬퍼 메서드
    // =================================================================
    private void removeNoticeFeed(Long teamId, Long noticeId) {
        String feedId = FeedIds.notice(teamId, noticeId);
        feedService.removeFeedItem(Set.of(teamId), feedId);
    }

    /* ===== Helpers ===== */

    /** 팀 존재 체크 */
    private Team requireTeam(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new BaseException(ErrorCode.GROUP_NOT_FOUND));
    }

    /** 열람 권한: 승인 멤버여야 함 */
    private void verifyApproved(Long teamId, Long memberId) {
        TeamMemberStatus rel = teamMemberStatusRepository
                .findByTeam_TeamIdAndMember_MemberId(teamId, memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCESS_DENIED));
        if (rel.getStatus() != ParticipationStatus.APPROVED) {
            throw new BaseException(ErrorCode.ACCESS_DENIED);
        }
    }

    /** 수정/삭제/생성 권한: 승인 + (리더/부리더) */
    private void verifyEditor(Long teamId, Long memberId) {
        TeamMemberStatus rel = teamMemberStatusRepository
                .findByTeam_TeamIdAndMember_MemberId(teamId, memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCESS_DENIED));

        if (rel.getStatus() != ParticipationStatus.APPROVED) {
            throw new BaseException(ErrorCode.ACCESS_DENIED);
        }
        TeamRole role = rel.getTeamRole();
        if (role != TeamRole.LEADER && role != TeamRole.SUBLEADER) {
            throw new BaseException(ErrorCode.ACCESS_DENIED);
        }
    }

    /** 해당 팀의 공지인지 체크 */
    private TeamNotice requireNoticeInTeam(Long teamId, Long noticeId) {
        return teamNoticeRepository
                .findByTeam_TeamIdAndTeamNoticeId(teamId, noticeId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOTICE_NOT_FOUND));
    }
}