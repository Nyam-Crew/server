package com.nyam.everyday.module.bookmark.repository;

import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.module.bookmark.dto.BookmarkAndBoardDto;
import com.nyam.everyday.module.bookmark.entity.Bookmark;
import com.nyam.everyday.module.member.entity.Member;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark,Long> {

  boolean existsByMemberAndBoard(Member member, Board board);

  @Query("""
  select new com.nyam.everyday.module.bookmark.dto.BookmarkAndBoardDto(
    br.boardId,
    b.bookmarkId,
    br.boardTitle,
    br.boardContent,
    m.nickname,
    br.likeCount,
    br.viewCount,
    br.commentCount,
    br.boardType,
    b.createdDate
  )
  from Bookmark b
  join b.board br
  join br.member m
  where b.member = :member
  order by b.createdDate desc, b.bookmarkId desc
""")
  Page<BookmarkAndBoardDto> findBookmarkedBoardsByMember(@Param("member") Member member, Pageable pageable);

  long deleteByMemberAndBoard(Member member, Board board);

  boolean existsByMember_MemberIdAndBoard_BoardId(Long memberId, Long boardId);

  Optional<Bookmark> findByMember_MemberIdAndBoard_BoardId(Long memberId, Long boardId);

  void deleteByMember_MemberIdAndBoard_BoardId(Long memberId, Long boardId);


  // BookmarkRepository
  List<Bookmark> findByMember_MemberIdAndBoard_BoardIdIn(Long memberId, Collection<Long> boardIds);
}
