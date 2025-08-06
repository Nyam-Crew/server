package com.nyam.everyday.module.team.repository;

import com.nyam.everyday.module.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author : 이지은
 * @fileName : TeamRepository
 * @since : 25. 8. 5.
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

}