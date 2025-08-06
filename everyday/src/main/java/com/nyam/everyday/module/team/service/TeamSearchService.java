package com.nyam.everyday.module.team.service;

import com.nyam.everyday.web.team.dto.TeamDto;
import com.nyam.everyday.web.team.dto.TeamSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 그룹 검색 확장 인터페이스
 * Todo. 추후 Elastic Search  확장 대비하여 작성만!
 *
 * @author : 이지은
 * @fileName : TeamSearchService
 * @since : 25. 8. 6.
 */
public interface TeamSearchService {
    Page<TeamDto> searchTeams(TeamSearchDto condition, Pageable pageable);
}
