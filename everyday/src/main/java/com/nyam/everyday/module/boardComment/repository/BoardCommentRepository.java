package com.nyam.everyday.module.boardComment.repository;

import com.nyam.everyday.module.boardComment.entity.BoardComment;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

  /** ✅ 루트 댓글만 (parent IS NULL) — 페이지 + countQuery 분리 */
  @Query(
      value = """
              select c
              from BoardComment c
              join fetch c.member m
              where c.board.boardId = :boardId
                and c.parent is null
              """,
      countQuery = """
              select count(c)
              from BoardComment c
              where c.board.boardId = :boardId
                and c.parent is null
              """
  )
  Page<BoardComment> findPageByBoardIdAndParentIsNull(@Param("boardId") Long boardId, Pageable pageable);

  /** ✅ 특정 댓글의 대댓글만 (parent_id = :parentId) — 페이지 + countQuery 분리 */
  @Query(
      value = """
              select c
              from BoardComment c
              join fetch c.member m
              where c.board.boardId = :boardId
                and c.parent.commentId = :parentId
              """,
      countQuery = """
              select count(c)
              from BoardComment c
              where c.board.boardId = :boardId
                and c.parent.commentId = :parentId
              """
  )
  Page<BoardComment> findPageByBoardIdAndParentId(@Param("boardId") Long boardId,
      @Param("parentId") Long parentId,
      Pageable pageable);

  /** ✅ 삭제/수정 권한 체크용 fetch join */
  @Query("""
         select c
         from BoardComment c
         join fetch c.member m
         join fetch c.board b
         where c.commentId = :id
         """)
  Optional<BoardComment> findByIdWithMemberAndBoard(@Param("id") Long id);

  /** ✅ 자식 존재 여부 */
  boolean existsByParent(BoardComment parent);

  // ====== ✅ childCount 계산용 Projection & 쿼리 ======
  interface ParentCount {
    Long getParentId();
    Long getCnt();
  }

  /**
   * ✅ 부모 댓글 id 묶음에 대해 각 부모별 대댓글 개수를 한 번에 조회
   */
  @Query("""
         select c.parent.commentId as parentId, count(c) as cnt
         from BoardComment c
         where c.parent.commentId in :parentIds
         group by c.parent.commentId
         """)
  List<ParentCount> countChildrenByParentIds(@Param("parentIds") Collection<Long> parentIds);
}