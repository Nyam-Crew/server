package com.nyam.everyday.module.team.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.module.team.enums.ParticipationStatus;
import com.nyam.everyday.module.team.enums.TeamRole;
import com.nyam.everyday.module.team.repository.TeamMemberStatusRepository;
import com.nyam.everyday.module.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 *
 * 그룹 멤버 관리 service
 *
 * @author : 이지은
 * @fileName : TeamMemberService
 * @since : 25. 8. 7.
 *
 */
@Service
@RequiredArgsConstructor
public class TeamMemberService {

    private final TeamRepository teamRepository;
    private final TeamMemberStatusRepository teamMemberStatusRepository;

    @Transactional
    public void updateMemberStatus(Long teamId, Long targetMemberId, ParticipationStatus newStatus, Long requesterId) {
        TeamMemberStatus targetStatus = teamMemberStatusRepository
                .findByTeam_TeamIdAndMember_MemberId(teamId, targetMemberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        if (targetStatus.getStatus() != ParticipationStatus.PENDING) {
            throw new BaseException(ErrorCode.ALREADY_PROCESSED);
        }

        TeamMemberStatus requesterStatus = teamMemberStatusRepository
                .findByTeam_TeamIdAndMember_MemberId(teamId, requesterId)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCESS_DENIED));

        if (!requesterStatus.getTeamRole().isManager()) {
            throw new BaseException(ErrorCode.ACCESS_DENIED);
        }

        if (newStatus == ParticipationStatus.APPROVED) {
            // 1) 정원 체크
            Team team = targetStatus.getTeam();
            if (team.getTeamCurrentMembers() >= team.getTeamMaxMembers()) {
                throw new BaseException(ErrorCode.TEAM_CAPACITY_FULL);
            }

            // 2) 상태 전환(가입 완료)
            targetStatus.approve();                 // 내부에서 status=APPROVED 로 세팅

            // 3) 인원수 +1 (APPROVED = 활성 멤버)
            team.increaseCurrentMembers(1);

        } else if (newStatus == ParticipationStatus.REJECTED) {
            targetStatus.reject();                  // status=REJECTED
            // 인원수 변화 없음
        } else {
            throw new BaseException(ErrorCode.INVALID_STATUS);
        }
    }

    @Transactional
    public void leaveTeam(Long teamId, Long memberId) {
        TeamMemberStatus memberStatus = teamMemberStatusRepository
                .findByTeam_TeamIdAndMember_MemberId(teamId, memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        // 방장은 나가기 불가
        if (memberStatus.getTeamRole() == TeamRole.LEADER) {
            throw new BaseException(ErrorCode.LEADER_CANNOT_LEAVE, "방장은 그룹을 탈퇴할 수 없습니다.");
        }

        // 이미 LEFT나 BANNED면 멱등 처리
        if (memberStatus.getStatus() == ParticipationStatus.LEFT ||
                memberStatus.getStatus() == ParticipationStatus.BANNED) {
            return;
        }

        boolean wasApproved = memberStatus.getStatus().isApproved();

        memberStatus.markLeft(LocalDateTime.now());

        if (wasApproved) {
            memberStatus.getTeam().decreaseCurrentMembers(1);
        }
    }

    // TODO(mvp-done): 권한 체크 공통화(TeamAuthorizer로 통합) 하기. 권한 체크 메서드를 따로 모아두고 호출하는 방식이 나아보인다.
    @Transactional
    public void ban(Long teamId, Long actorMemberId, Long targetMemberId, String reason) {
        // 팀 존재 확인(정합성)
        teamRepository.findById(teamId)
                .orElseThrow(() -> new BaseException(ErrorCode.GROUP_NOT_FOUND));

        TeamMemberStatus actor = teamMemberStatusRepository
                .findByTeam_TeamIdAndMember_MemberId(teamId, actorMemberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        // 방장만 가능
        if (!actor.getTeamRole().isLeader()) {
            throw new BaseException(ErrorCode.ONLY_LEADER_CAN_BAN);
        }
        // 자기 자신 금지
        if (actorMemberId.equals(targetMemberId)) {
            throw new BaseException(ErrorCode.INVALID_REQUEST, "자기 자신은 강퇴할 수 없습니다.");
        }

        TeamMemberStatus target = teamMemberStatusRepository
                .findByTeam_TeamIdAndMember_MemberId(teamId, targetMemberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        // 리더/부리더 보호
        if (target.getTeamRole().isLeader() || target.getTeamRole().isSubLeader()) {
            throw new BaseException(ErrorCode.LEADER_CANNOT_BAN);
        }

        // 이미 BANNED면 멱등 처리
        if (target.getStatus() == ParticipationStatus.BANNED) return;

        // 승인(=활성 멤버)이었다면 -1
        boolean wasApproved = target.getStatus().isApproved();

        // 상태 전환 + 사유 저장
        target.ban(reason, LocalDateTime.now());

        if (wasApproved) {
            target.getTeam().decreaseCurrentMembers(1);
        }

    }

    // 아래 기능 여기로 위치 이동할 예정
    // 다음 기능도 여기에 추가될 예정:
    // - 강퇴
    // - 부방장 역할 부여/회수
    // - 방장 위임
}