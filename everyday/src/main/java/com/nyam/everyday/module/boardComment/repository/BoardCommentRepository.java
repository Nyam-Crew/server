package com.nyam.everyday.module.boardComment.repository;

import com.nyam.everyday.module.boardComment.entity.BoardComment;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {


  // 특정 게시글의 댓글/대댓글을 페이지로 조회
  @Query(
      value = """
        select c
        from BoardComment c
            join fetch c.member m
            join fetch c.board b
            left join fetch c.parent p
        where b.boardId = :boardId
        order by c.createdDate asc
        """,
      countQuery = """
          select count(c)
          from BoardComment c
          where c.board.boardId = :boardId
          """
  )
  Page<BoardComment> findPageByBoardIdWithJoins(@Param("boardId") Long boardId, Pageable pageable);


  //자식(대댓글) 존재여부 확인 : 부모 댓글 삭제 정책 결정에 사용
  boolean existsByParent(BoardComment parent);

  // 삭제/권한 검증용 단건 조회(작성자/게시글까지 함께 로드)
  @Query("""
      select c
      from BoardComment c
        join fetch c.member m
        join fetch c.board  b
      where c.commentId = :commentId
      """)
  Optional<BoardComment> findByIdWithMemberAndBoard(@Param("commentId") Long commentId);
}
