package com.nyam.everyday.module.boardLike.repository;

import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.module.boardLike.entity.BoardLike;
import com.nyam.everyday.module.member.entity.Member;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {

  boolean existsByMemberAndBoard(Member member, Board board);

  long deleteByMemberAndBoard(Member member, Board board);

  long countByBoard(Board board);

  long countByMember_MemberId(Long memberId);



  // 배치 상태 조회 (내가 누른 것들만)
  List<BoardLike> findAllByMemberAndBoardIn(Member member, Collection<Board> boards);


}
