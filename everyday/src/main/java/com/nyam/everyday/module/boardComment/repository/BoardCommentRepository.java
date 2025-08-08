package com.nyam.everyday.module.boardComment.repository;

import com.nyam.everyday.module.boardComment.entity.BoardComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

}
