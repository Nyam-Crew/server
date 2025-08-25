package com.nyam.everyday.module.team.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.module.team.enums.ParticipationStatus;
import com.nyam.everyday.module.team.enums.TeamRole;
import com.nyam.everyday.module.team.repository.TeamMemberStatusRepository;
import com.nyam.everyday.module.team.repository.TeamRepository;
import com.nyam.everyday.web.team.dto.MemberTeamListDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.nyam.everyday.web.team.dto.TeamDetailDto;
import com.nyam.everyday.web.team.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 그룹 멤버 관리 service
 *
 * @author : 이지은
 * @fileName : TeamMemberService
 * @since : 25. 8. 7.
 */
@Service
@RequiredArgsConstructor
public class TeamMemberService {

  private final TeamRepository teamRepository;
  private final TeamMemberStatusRepository teamMemberStatusRepository;
  private final TeamMapper teamMapper;

  @Transactional
  public void updateMemberStatus(Long teamId, Long targetMemberId, ParticipationStatus newStatus,
      Long requesterId) {
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
    if (target.getStatus() == ParticipationStatus.BANNED) {
      return;
    }

    // 승인(=활성 멤버)이었다면 -1
    boolean wasApproved = target.getStatus().isApproved();

    // 상태 전환 + 사유 저장
    target.ban(reason, LocalDateTime.now());

    if (wasApproved) {
      target.getTeam().decreaseCurrentMembers(1);
    }

  }

  @Transactional
  public void transferLeader(Long teamId, Long actorMemberId, Long targetMemberId) {

    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new BaseException(ErrorCode.GROUP_NOT_FOUND));

    TeamMemberStatus actor = teamMemberStatusRepository
        .findByTeam_TeamIdAndMember_MemberId(teamId, actorMemberId)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

    // 리더만 가능 + 승인 상태만 허용
    if (!actor.getTeamRole().isLeader() || actor.getStatus() != ParticipationStatus.APPROVED) {
      throw new BaseException(ErrorCode.ACCESS_DENIED);
    }
    // 자기 자신으로 위임 불가
    if (actorMemberId.equals(targetMemberId)) {
      throw new BaseException(ErrorCode.INVALID_REQUEST, "자기 자신에게는 위임할 수 없습니다.");
    }

    TeamMemberStatus target = teamMemberStatusRepository
        .findByTeam_TeamIdAndMember_MemberId(teamId, targetMemberId)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

    if (target.getStatus() != ParticipationStatus.APPROVED) {
      throw new BaseException(ErrorCode.INVALID_REQUEST, "승인된 멤버에게만 위임할 수 있습니다.");
    }

    // 역할 스왑: target -> LEADER, actor(기존 리더) -> SUBLEADER
    target.changeRole(TeamRole.LEADER);
    actor.changeRole(TeamRole.SUBLEADER);

    // 팀의 Leader 갱신(팀 엔티티에 owner/member FK가 있다면)
    team.changeLeader(target.getMember());
    // JPA 변경감지로 커밋 시점에 반영
  }

  @Transactional
  public void changeRole(Long teamId, Long actorMemberId, Long targetMemberId, TeamRole newRole) {
    // 팀/행위자
    teamRepository.findById(teamId)
        .orElseThrow(() -> new BaseException(ErrorCode.GROUP_NOT_FOUND));

    TeamMemberStatus actor = teamMemberStatusRepository
        .findByTeam_TeamIdAndMember_MemberId(teamId, actorMemberId)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

    if (!actor.getTeamRole().isLeader() || actor.getStatus() != ParticipationStatus.APPROVED) {
      throw new BaseException(ErrorCode.ACCESS_DENIED);
    }

    // 리더 변경은 별도 API 사용
    if (newRole == TeamRole.LEADER) {
      throw new BaseException(ErrorCode.INVALID_REQUEST, "방장 변경은 /leader API를 사용하세요.");
    }

    // 자기 자신 역할 변경 방지
    if (actorMemberId.equals(targetMemberId)) {
      throw new BaseException(ErrorCode.INVALID_REQUEST, "본인 역할은 변경할 수 없습니다.");
    }

    TeamMemberStatus target = teamMemberStatusRepository
        .findByTeam_TeamIdAndMember_MemberId(teamId, targetMemberId)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

    if (target.getStatus() != ParticipationStatus.APPROVED) {
      throw new BaseException(ErrorCode.INVALID_REQUEST, "승인된 멤버만 역할을 변경할 수 있습니다.");
    }

    // 기존 LEADER는 여기서 바꾸지 않음
    if (target.getTeamRole().isLeader()) {
      throw new BaseException(ErrorCode.INVALID_REQUEST, "방장 역할 변경은 /leader API로만 가능합니다.");
    }

    // 허용되는 변경: SUBLEADER <-> MEMBER
    if (newRole != TeamRole.SUBLEADER && newRole != TeamRole.MEMBER) {
      throw new BaseException(ErrorCode.INVALID_REQUEST, "허용되지 않은 역할입니다.");
    }

    target.changeRole(newRole);
  }

  /// 2025.08.18 정호종
  // 어떤 유저가 그룹의 멤버인지 확인하기 위한 함수
  @Transactional(readOnly = true)
  public Boolean isMember(Long memberId, Long teamId) {
    return teamMemberStatusRepository.existsByTeam_TeamIdAndMember_MemberIdAndStatus(memberId,
        teamId, ParticipationStatus.APPROVED);
  }

  // 특정 유저가 속한 그룹 리스트를 반환받는다
  @Transactional(readOnly = true)
  public MemberTeamListDto getAllGroupsByMemberId(Long memberId) {
    List<TeamMemberStatus> tmp = teamMemberStatusRepository.getAllByMember_MemberId(memberId);

    return MemberTeamListDto.of(tmp);
  }

  @Transactional(readOnly = true)
  public Set<Long> findTeamIdsByMember(Long memberId) {
      return teamMemberStatusRepository.findActiveTeamIdsByMemberId(memberId);
  }

  @Transactional
  public List<TeamDetailDto> getMyPageGroups(Long memberId) {
    // 1. 현재 사용자와 관련된 모든 참여 상태 정보를 가져옵니다.
    List<TeamMemberStatus> allMyStatuses = teamMemberStatusRepository.getAllByMember_MemberId(memberId);

    // 2. 각 참여 상태(TeamMemberStatus)를 TeamDetailDto로 변환합니다.
    return allMyStatuses.stream()
            .map(status -> {
              Team team = status.getTeam();
              // TeamMapper의 기존 toDetailDto 메소드를 재활용합니다.
              // 이 메소드가 필요로 하는 추가 파라미터들을 status와 team 객체에서 가져와 전달합니다.
              // subLeaderNickname은 별도 조회가 필요하므로 일단 null로 전달합니다.
              return teamMapper.toDetailDto(
                      team,
                      status.getStatus(),
                      status.getTeamRole(),
                      team.getOwner().getNickname(),
                      null // subLeaderNickname
              );
            })
            .collect(Collectors.toList());
  }
}