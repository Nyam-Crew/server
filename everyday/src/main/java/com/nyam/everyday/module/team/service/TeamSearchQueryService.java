package com.nyam.everyday.module.team.service;

import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.module.team.repository.TeamRepository;
import com.nyam.everyday.web.team.dto.TeamDto;
import com.nyam.everyday.web.team.dto.TeamSearchDto;
import com.nyam.everyday.web.team.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 그룹 검색 확장 service, 현재 사용x
 * Todo. 추후 Elastic Search  확장 대비하여 작성만!
 *
 * @author : 이지은
 * @fileName : TeamSearchQueryService
 * @since : 25. 8. 6.
 */
@Service
@RequiredArgsConstructor
public class TeamSearchQueryService implements TeamSearchService {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;

    @Override
    public Page<TeamDto> searchTeams(TeamSearchDto searchDto, Pageable pageable) {
        Page<Team> teams;

        // 현재는 keyword만 처리, 추후 category, sort 확장 가능
        if (searchDto.getKeyword() != null && !searchDto.getKeyword().isBlank()) {
            teams = teamRepository.findByTeamNameContainingIgnoreCase(searchDto.getKeyword(), pageable);
        } else {
            teams = teamRepository.findAll(pageable);
        }

        return teams.map(teamMapper::toDto);
    }
}