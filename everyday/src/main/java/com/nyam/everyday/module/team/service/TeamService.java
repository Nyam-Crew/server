package com.nyam.everyday.module.team.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.module.team.repository.TeamMemberStatusRepository;
import com.nyam.everyday.module.team.repository.TeamRepository;
import com.nyam.everyday.web.team.dto.TeamDetailDto;
import com.nyam.everyday.web.team.dto.TeamDto;
import com.nyam.everyday.web.team.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

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


    public TeamDto createTeam(TeamDto dto, Long memberId) {
        //memberId에 대한 유효성 검사
        Member owner = memberRepository.findById(memberId).orElseThrow(()
                -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        //Mapstruct builder
        Team team = teamMapper.toEntity(dto, owner); // DTO → Entity 변환

        teamRepository.save(team);

        return teamMapper.toDto(team);
    }

    public Page<TeamDto> getTeamList(String keyword, Pageable pageable) {
        Page<Team> teams;

        if (keyword != null && !keyword.isBlank()) {
            teams = teamRepository.findByTeamTitleContainingIgnoreCase(keyword, pageable);
        } else {
            teams = teamRepository.findAll(pageable);
        }

        return teams.map(teamMapper::toDto);
    }

    public TeamDetailDto getTeam(Long teamId, Long memberId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BaseException(ErrorCode.GROUP_NOT_FOUND));

        Optional<TeamMemberStatus> memberStatusOpt =
                teamMemberStatusRepository.findByTeam_TeamIdAndMember_MemberId(teamId, memberId);

        String participationStatus = memberStatusOpt
                .map(TeamMemberStatus::getStatus)
                .orElse("NOT_JOINED");

        String teamRole = memberStatusOpt
                .map(TeamMemberStatus::getTeamRole)
                .orElse(null);

        return TeamDetailDto.builder()
                .teamId(team.getTeamId())
                .teamTitle(team.getTeamTitle())
                .teamDescription(team.getTeamDescription())
                .teamImage(team.getTeamImg())
                .currentMemberCount(team.getTeamCurrentMembers())
                .maxMembers(team.getTeamMaxMembers())
                .createdDate(team.getCreatedDate().toString())
                .status(TeamDetailDto.ParticipationStatus.valueOf(participationStatus))
                .teamRole(TeamDetailDto.TeamRole.valueOf(teamRole))
                .build();
    }
}