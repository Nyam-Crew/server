package com.nyam.everyday.module.bookmark.repository;

import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.module.bookmark.dto.BookmarkAndBoardDto;
import com.nyam.everyday.module.bookmark.entity.Bookmark;
import com.nyam.everyday.module.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark,Long> {

  boolean existsByMemberAndBoard(Member member, Board board);


  @Query(value = "SELECT new com.nyam.everyday.module.bookmark.dto.BookmarkAndBoardDto(b.board,b.bookmarkId, b.createdDate) " +
      "FROM Bookmark b " +
      "JOIN b.board.member m " +
      "WHERE b.member = :member",
      countQuery = "SELECT count(b) FROM Bookmark b WHERE b.member = :member")
  Page<BookmarkAndBoardDto> findBookmarkedBoardsByMember(@Param("member") Member member, Pageable pageable);

  long deleteByMemberAndBoard(Member member, Board board);
}
