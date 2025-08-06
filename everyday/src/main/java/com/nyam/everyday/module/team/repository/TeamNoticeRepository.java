package com.nyam.everyday.module.team.repository;

import com.nyam.everyday.module.team.entity.TeamNotice;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 그룹 공지사항 Repository
 *
 * @author : 이지은
 * @fileName : TeamNoticeRepository
 * @since : 25. 8. 6.
 */
public interface TeamNoticeRepository extends JpaRepository<TeamNotice, Long> {

}
