package com.nyam.everyday.module.team.repository;

import com.nyam.everyday.module.team.entity.TeamNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 그룹 공지사항 Repository
 *
 * @author : 이지은
 * @fileName : TeamNoticeRepository
 * @since : 25. 8. 6.
 */
public interface TeamNoticeRepository extends JpaRepository<TeamNotice, Long> {

    // 팀의 공지(단일) 조회
    Optional<TeamNotice> findByTeam_TeamId(Long teamId);

    //팀에 공지가 존재하는지 확인
    boolean existsByTeam_TeamId(Long teamId);

    // 생성 시 소유 검증/조회
    Optional<TeamNotice> findByTeam_TeamIdAndTeamNoticeId(Long teamId, Long teamNoticeId);

}
