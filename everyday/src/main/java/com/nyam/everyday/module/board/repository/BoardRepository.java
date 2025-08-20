package com.nyam.everyday.module.board.repository;

import com.nyam.everyday.module.board.dto.BoardWithNicknameDto;
import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.web.board.dto.BoardPageDto;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board,Long> {


  @Query("SELECT b FROM Board b JOIN FETCH b.member WHERE b.boardId = :boardId")
  Optional<Board> findWithMemberByBoardId(@Param("boardId") Long boardId);


  @Query("SELECT new com.nyam.everyday.web.board.dto.BoardPageDto(" +
      "b.boardId, b.boardTitle, m.nickname, b.boardType, b.createdDate, b.viewCount, b.commentCount,b.likeCount) " +
      "FROM Board b JOIN b.member m " +
      "WHERE (:boardType IS NULL OR b.boardType = :boardType)" +
      "ORDER BY b.likeCount DESC ")
  Page<BoardPageDto> findBoardPreviews(@Param("boardType") String boardType, Pageable pageable);



  @Query(value = """
    select new com.nyam.everyday.module.board.dto.BoardWithNicknameDto(
      b.boardId, b.boardTitle, m.nickname, b.boardType, b.createdDate,
      b.viewCount, b.commentCount, b.likeCount
    )
    from Board b
    join b.member m
    where m.memberId = :memberId
    order by b.createdDate desc
    """,
      countQuery = """
    select count(b.boardId)
    from Board b
    where b.member.memberId = :memberId
    """
  )
  Page<BoardWithNicknameDto> findByMemberId(@Param("memberId") Long memberId, Pageable pageable);



}
