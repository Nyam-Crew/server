package com.nyam.everyday.module.team.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.module.team.entity.TeamNotice;
import com.nyam.everyday.module.team.enums.ParticipationStatus;
import com.nyam.everyday.module.team.enums.TeamRole;
import com.nyam.everyday.module.team.repository.TeamMemberStatusRepository;
import com.nyam.everyday.module.team.repository.TeamNoticeRepository;
import com.nyam.everyday.module.team.repository.TeamRepository;
import com.nyam.everyday.web.team.dto.TeamNoticeCreatedDto;
import com.nyam.everyday.web.team.dto.TeamNoticeDto;
import com.nyam.everyday.web.team.dto.TeamNoticeUpdateDto;
import com.nyam.everyday.web.team.mapper.TeamNoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public TeamNoticeDto createNotice(Long teamId, Long actorMemberId, TeamNoticeCreatedDto req) {
        Team team = requireTeam(teamId);
        verifyEditor(teamId, actorMemberId);

        // 팀당 1건 제한 (MVP: DB 제약 없음, 서비스 레벨만)
        if (teamNoticeRepository.existsByTeam_TeamId(teamId)) {
            throw new BaseException(ErrorCode.NOTICE_ALREADY_EXISTS,
                    "이미 등록된 공지가 있습니다. 기존 공지를 삭제한 뒤 다시 시도해주세요.");
        }

        Member writer = memberRepository.findById(actorMemberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        TeamNotice saved = teamNoticeRepository.save(
                TeamNotice.builder()
                        .team(team)
                        .member(writer)
                        .title(req.getTitle())
                        .content(req.getContent())
                        .build()
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
        return teamNoticeMapper.toNoticeDTO(notice);
    }

    @Transactional(readOnly = true)
    public TeamNoticeDto getNoticeByTeam(Long teamId, Long actorMemberId) {
        verifyApproved(teamId, actorMemberId);
        TeamNotice notice = teamNoticeRepository.findByTeam_TeamId(teamId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOTICE_NOT_FOUND));
        return teamNoticeMapper.toNoticeDTO(notice);
    }

    @Transactional
    public void deleteNotice(Long teamId, Long noticeId, Long actorMemberId) {
        verifyEditor(teamId, actorMemberId);

        TeamNotice notice = teamNoticeRepository
                .findByTeam_TeamIdAndTeamNoticeId(teamId, noticeId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOTICE_NOT_FOUND));

        // 하드 삭제
        teamNoticeRepository.delete(notice);
        // 첨부/댓글 등 연관 데이터는 FK ON DELETE CASCADE 권장
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