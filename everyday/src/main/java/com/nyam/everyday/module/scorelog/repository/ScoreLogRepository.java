package com.nyam.everyday.module.scorelog.repository;

import com.nyam.everyday.module.scorelog.entity.ScoreLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreLogRepository extends JpaRepository<ScoreLog, Long> {
}
