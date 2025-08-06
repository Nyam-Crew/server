package com.nyam.everyday.module.team.repository;

import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 그룹 멤버 현황 관련 Repository
 *
 * @author : 이지은
 * @fileName : TeamMemberStatusRepository
 * @since : 25. 8. 6.
 */
@Repository
public interface TeamMemberStatusRepository extends JpaRepository<TeamMemberStatus, Long> {

}
