package com.nyam.everyday.module.bookmark.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.module.board.repository.BoardRepository;
import com.nyam.everyday.module.bookmark.dto.BookmarkAndBoardDto;
import com.nyam.everyday.module.bookmark.entity.Bookmark;
import com.nyam.everyday.module.bookmark.repository.BookmarkRepository;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.web.bookmark.mapper.BookmarkMapper;
import com.nyam.everyday.web.bookmark.dto.CreateBookmarkRequestDto;
import com.nyam.everyday.web.bookmark.dto.CreateBookmarkResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkService {

  private final BookmarkRepository bookmarkRepository;
  private final MemberRepository memberRepository;
  private final BoardRepository boardRepository;
  private final BookmarkMapper bookmarkMapper;


  @Transactional
  public CreateBookmarkResponseDto createBookmark(Long memberId, CreateBookmarkRequestDto dto) {
    // 1.작성자 및 게시글 조회
    Member member = memberRepository.findById(memberId)
        .orElseThrow(()->new BaseException(ErrorCode.MEMBER_NOT_FOUND));

    Board board = boardRepository.findById(dto.getBoardId())
        .orElseThrow(()->new BaseException(ErrorCode.BOARD_NOT_FOUND));

    // 2.중복 방지
    if(bookmarkRepository.existsByMemberAndBoard(member, board)){
      throw new BaseException(ErrorCode.ALREADY_IN_BOOKMARK);
    }
    // 3.저장
    Bookmark saved = bookmarkRepository.save(
        bookmarkMapper.toEntity(dto,board,member)
    );
    // 4.응답 DTO: mapper -> tobuilder로 최종상태만 세팅
    CreateBookmarkResponseDto res = bookmarkMapper.toResponseDto(saved);
    return res.toBuilder()
        .bookmarked(true)
        .build();


  }

  @Transactional(readOnly = true)
  public Page<BookmarkAndBoardDto> getMyBookmarks(Long memberId, Pageable pageable) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));
    return  bookmarkRepository.findBookmarkedBoardsByMember(member, pageable);
  }


}
