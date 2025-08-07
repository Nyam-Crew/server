package com.nyam.everyday.module.team.service;

import com.nyam.everyday.common.aws.s3.service.AwsS3Service;
import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.awsS3.dto.AwsS3Response;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.module.team.repository.TeamMemberStatusRepository;
import com.nyam.everyday.module.team.repository.TeamRepository;
import com.nyam.everyday.web.team.dto.MemberStatusUpdateDto;
import com.nyam.everyday.web.team.dto.TeamDetailDto;
import com.nyam.everyday.web.team.dto.TeamDto;
import com.nyam.everyday.web.team.dto.TeamMemberStatusDto;
import com.nyam.everyday.web.team.mapper.TeamMapper;
import com.nyam.everyday.web.team.mapper.TeamMemberStatusMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.nyam.everyday.common.util.EnumUtils.safeValueOf;

/**
 * @author : 이지은
 * @fileName : TeamService
 * @since : 25. 8. 5.
 */
@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final TeamMemberStatusRepository teamMemberStatusRepository;
    private final TeamMapper teamMapper;
    private final TeamMemberStatusMapper teamMemberStatusMapper;
    private final AwsS3Service awsS3Service;

    @Transactional
    public TeamDto createTeam(TeamDto dto,/* MultipartFile imageFile,*/ Long memberId) {
        //memberId에 대한 유효성 검사
        Member owner = memberRepository.findById(memberId).orElseThrow(()
                -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

//        String imageUrl = null;
//        if (imageFile != null && !imageFile.isEmpty()) {
//            AwsS3Response response = awsS3Service.uploadFile(imageFile);
//            imageUrl = response.getUrl();
//            dto.setTeamImg(imageUrl); // 업로드된 이미지 URL DTO에 주입
//        }

        //Mapstruct builder
        Team team = teamMapper.toEntity(dto, owner); // DTO → Entity 변환

        teamRepository.save(team);

        registerLeader(team, owner);

        return teamMapper.toDto(team);
    }

    @Transactional
    public void registerLeader(Team team, Member owner) {
        TeamMemberStatusDto leaderDto = TeamMemberStatusDto.builder()
                .status(TeamMemberStatusDto.ParticipationStatus.APPROVED)
                .teamRole(TeamMemberStatusDto.TeamRole.LEADER)
                .build();

        TeamMemberStatus leaderStatus = teamMemberStatusMapper.toEntity(leaderDto, team, owner);
        teamMemberStatusRepository.save(leaderStatus);
    }

    @Transactional
    public Page<TeamDto> getTeamList(String keyword, Pageable pageable) {
        Page<Team> teams;

        if (keyword != null && !keyword.isBlank()) {
            teams = teamRepository.findByTeamTitleContainingIgnoreCase(keyword, pageable);
        } else {
            teams = teamRepository.findAll(pageable);
        }

        return teams.map(teamMapper::toDto);
    }

    @Transactional
    public TeamDetailDto getTeam(Long teamId, Long memberId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BaseException(ErrorCode.GROUP_NOT_FOUND));

        Optional<TeamMemberStatus> memberStatusOpt =
                teamMemberStatusRepository.findByTeam_TeamIdAndMember_MemberId(teamId, memberId);

        TeamDetailDto.ParticipationStatus participationStatus = memberStatusOpt
                .map(s -> TeamDetailDto.ParticipationStatus.valueOf(s.getStatus().name()))
                .orElse(TeamDetailDto.ParticipationStatus.NOT_JOINED);

        TeamDetailDto.TeamRole teamRole = memberStatusOpt
                .map(s -> TeamDetailDto.TeamRole.valueOf(s.getTeamRole().name()))
                .orElse(null);

        return teamMapper.toDetailDto(team, participationStatus, teamRole);
    }

    @Transactional
    public void requestToJoin(Long teamId, Long memberId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BaseException(ErrorCode.GROUP_NOT_FOUND));

        // 중복 신청 또는 가입 여부 확인
        Optional<TeamMemberStatus> existing = teamMemberStatusRepository
                .findByTeam_TeamIdAndMember_MemberId(teamId, memberId);

        if (existing.isPresent()) {
            TeamMemberStatus status = existing.get();
            if (status.getStatus() == TeamMemberStatus.ParticipationStatus.APPROVED) {
                throw new BaseException(ErrorCode.ALREADY_JOINED_GROUP);
            } else if (status.getStatus() == TeamMemberStatus.ParticipationStatus.PENDING) {
                throw new BaseException(ErrorCode.ALREADY_EXIST_JOIN);
            }
        }

        // 정원 초과 체크
        if (team.getTeamCurrentMembers() >= team.getTeamMaxMembers()) {
            throw new BaseException(ErrorCode.TEAM_CAPACITY_FULL);
        }

        // 참가 요청 저장
        TeamMemberStatus request = TeamMemberStatus.builder()
                .team(team)
                .member(memberRepository.findById(memberId)
                        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND)))
                .status(TeamMemberStatus.ParticipationStatus.PENDING)
                .teamRole(TeamMemberStatus.TeamRole.MEMBER)
                .build();

        teamMemberStatusRepository.save(request);
    }

    public List<TeamMemberStatusDto> getJoinRequestMembers(Long teamId, Long requesterId) {
        // 권한 확인 (방장/부방장만 조회 가능)
        TeamMemberStatus requesterStatus = teamMemberStatusRepository
                .findByTeam_TeamIdAndMember_MemberId(teamId, requesterId)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCESS_DENIED));

        if (!requesterStatus.getTeamRole().isManager()) {
            throw new BaseException(ErrorCode.ACCESS_DENIED);
        }

        List<TeamMemberStatus> pendingMembers = teamMemberStatusRepository
                .findAllByTeam_TeamIdAndStatus(teamId, TeamMemberStatus.ParticipationStatus.PENDING);

        return teamMemberStatusMapper.toDtoList(pendingMembers);
    }

    @Transactional
    public void updateMemberStatus(Long teamId, Long targetMemberId, TeamMemberStatus.ParticipationStatus newStatus, Long requesterId) {
        //실제 신청한 member가 있는지 확인
        TeamMemberStatus targetStatus = teamMemberStatusRepository
                .findByTeam_TeamIdAndMember_MemberId(teamId, targetMemberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        //신청중 상태가 아니면 이미 처리한 요청에 대한 것은 exception
        if (targetStatus.getStatus() != TeamMemberStatus.ParticipationStatus.PENDING) {
            throw new BaseException(ErrorCode.ALREADY_PROCESSED);
        }

        TeamMemberStatus requesterStatus = teamMemberStatusRepository
                .findByTeam_TeamIdAndMember_MemberId(teamId, requesterId)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCESS_DENIED));

        if (!requesterStatus.getTeamRole().isManager()) {
            throw new BaseException(ErrorCode.ACCESS_DENIED);
        }

        // ✅ Entity enum 기준으로 비교
        if (newStatus == TeamMemberStatus.ParticipationStatus.APPROVED) {
            targetStatus.approve();
        } else if (newStatus == TeamMemberStatus.ParticipationStatus.REJECTED) {
            targetStatus.reject();
        } else {
            throw new BaseException(ErrorCode.INVALID_STATUS);
        }
    }

}