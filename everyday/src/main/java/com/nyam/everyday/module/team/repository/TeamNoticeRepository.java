package com.nyam.everyday.module.team.repository;

import com.nyam.everyday.module.team.entity.TeamNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 그룹 공지사항 Repository
 *
 * @author : 이지은
 * @fileName : TeamNoticeRepository
 * @since : 25. 8. 6.
 */
public interface TeamNoticeRepository extends JpaRepository<TeamNotice, Long> {
    @Modifying
    @Query("delete from TeamNotice n where n.team.teamId = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);
}
