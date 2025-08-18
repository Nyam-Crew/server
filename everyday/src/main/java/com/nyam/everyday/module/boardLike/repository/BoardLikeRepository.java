package com.nyam.everyday.module.boardLike.repository;

import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.module.boardLike.entity.BoardLike;
import com.nyam.everyday.module.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {

  boolean existsByMemberAndBoard(Member member, Board board);

  long deleteByMemberAndBoard(Member member, Board board);

  long countByBoard(Board board);

}
