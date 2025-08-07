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
                .status("APPROVED")
                .teamRole("LEADER")
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

        // String → Enum 안전 변환
        TeamDetailDto.ParticipationStatus participationStatus = memberStatusOpt
                .map(s -> safeValueOf(
                        TeamDetailDto.ParticipationStatus.class,
                        s.getStatus(),
                        TeamDetailDto.ParticipationStatus.NOT_JOINED
                ))
                .orElse(TeamDetailDto.ParticipationStatus.NOT_JOINED);

        TeamDetailDto.TeamRole teamRole = memberStatusOpt
                .map(s -> safeValueOf(
                        TeamDetailDto.TeamRole.class,
                        s.getTeamRole(),
                        null
                ))
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
            if (status.getStatus().equals("APPROVED")) {
                throw new BaseException(ErrorCode.ALREADY_JOINED_GROUP);
            } else if (status.getStatus().equals("PENDING")) {
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
                .status("PENDING")
                .teamRole("MEMBER")
                .build();

        teamMemberStatusRepository.save(request);
    }
}