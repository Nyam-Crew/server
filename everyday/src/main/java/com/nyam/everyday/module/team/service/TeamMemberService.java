package com.nyam.everyday.module.team.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.module.team.enums.ParticipationStatus;
import com.nyam.everyday.module.team.repository.TeamMemberStatusRepository;
import com.nyam.everyday.web.team.dto.MemberTeamListDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            targetStatus.approve();
        } else if (newStatus == ParticipationStatus.REJECTED) {
            targetStatus.reject();
        } else {
            throw new BaseException(ErrorCode.INVALID_STATUS);
        }
    }

    @Transactional
    public void leaveTeam(Long teamId, Long memberId) {
        TeamMemberStatus memberStatus = teamMemberStatusRepository
                .findByTeam_TeamIdAndMember_MemberId(teamId, memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        teamMemberStatusRepository.delete(memberStatus);
    }

    // 어떤 유저가 그룹의 멤버인지 확인하기 위한 함수
    @Transactional(readOnly = true)
    public Boolean isMember(Long memberid, Long teamId) {
        return teamMemberStatusRepository.existsByTeam_TeamIdAndMember_MemberIdAndStatus(memberid, teamId, ParticipationStatus.APPROVED);
    }

    // 특정 유저가 속한 그룹 리스트를 반환받는다
    @Transactional(readOnly = true)
    public MemberTeamListDto getAllGroupsByMemberId(Long memberId) {
        List<TeamMemberStatus> tmp = teamMemberStatusRepository.getAllByMember_MemberId(memberId);

        return MemberTeamListDto.of(tmp);
    }

    // 아래 기능 여기로 위치 이동할 예정
    // - 참가 승인/거절
    // 다음 기능도 여기에 추가될 예정:
    // - 강퇴
    // - 부방장 역할 부여/회수
    // - 방장 위임
}