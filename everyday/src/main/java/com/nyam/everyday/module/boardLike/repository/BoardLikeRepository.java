package com.nyam.everyday.module.boardLike.repository;

import com.nyam.everyday.module.boardLike.entity.BoardLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {

}
