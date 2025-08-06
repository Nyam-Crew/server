package com.nyam.everyday.module.team.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.module.team.repository.TeamRepository;
import com.nyam.everyday.web.team.dto.TeamDto;
import com.nyam.everyday.web.team.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}