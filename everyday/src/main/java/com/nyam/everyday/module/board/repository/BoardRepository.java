package com.nyam.everyday.module.board.repository;

import com.nyam.everyday.module.board.dto.BoardWithNicknameDto;
import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.web.board.dto.BoardPageDto;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board,Long> {


  @Query("SELECT b FROM Board b JOIN FETCH b.member WHERE b.boardId = :boardId")
  Optional<Board> findWithMemberByBoardId(@Param("boardId") Long boardId);


  @Query("SELECT new com.nyam.everyday.web.board.dto.BoardPageDto(" +
      "b.boardId, b.boardTitle, m.nickname, b.boardType, b.createdDate, b.viewCount, b.commentCount,b.likeCount) " +
      "FROM Board b JOIN b.member m " +
      "WHERE (:boardType IS NULL OR b.boardType = :boardType) " +
      "ORDER BY b.likeCount DESC ")
  Page<BoardPageDto> findBoardPreviews(@Param("boardType") String boardType, Pageable pageable);


  //검색 기능을 위해 닉네임/제목/내용으로 검색로직 하는 리포지토리
  @EntityGraph(attributePaths = "member")
  @Query(
      value = """
        select b
        from Board b
        join b.member m
        where (:type is null or b.boardType = :type)
          and (
                (:inTitle   = true and lower(b.boardTitle)   like lower(concat('%', :q, '%')) escape '!')
             or (:inContent = true and lower(b.boardContent) like lower(concat('%', :q, '%')) escape '!')
             or (:inNick    = true and lower(m.nickname)     like lower(concat('%', :q, '%')) escape '!')
          )
        """,
      countQuery = """
        select count(b)
        from Board b
        join b.member m
        where (:type is null or b.boardType = :type)
          and (
                (:inTitle   = true and lower(b.boardTitle)   like lower(concat('%', :q, '%')) escape '!')
             or (:inContent = true and lower(b.boardContent) like lower(concat('%', :q, '%')) escape '!')
             or (:inNick    = true and lower(m.nickname)     like lower(concat('%', :q, '%')) escape '!')
          )
        """
  )
  Page<Board> searchByField(
      @Param("q") String q, //검색어
      @Param("type") String type,//게시판 타입
      @Param("inTitle") boolean inTitle,//제목에서 검색 여부
      @Param("inContent") boolean inContent,//내용에서 검색 여부
      @Param("inNick") boolean inNick,//닉네임에서 검색 여부
      Pageable pageable
  );

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
