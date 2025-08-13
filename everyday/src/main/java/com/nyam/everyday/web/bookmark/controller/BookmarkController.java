package com.nyam.everyday.web.bookmark.controller;

import com.nyam.everyday.common.dto.CustomPageResponseDto;
import com.nyam.everyday.module.bookmark.dto.BookmarkAndBoardDto;
import com.nyam.everyday.module.bookmark.service.BookmarkService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.bookmark.dto.CreateBookmarkRequestDto;
import com.nyam.everyday.web.bookmark.dto.CreateBookmarkResponseDto;
import com.nyam.everyday.web.bookmark.dto.MyBookmarkListResponseDto;
import com.nyam.everyday.web.bookmark.mapper.BookmarkMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Bookmark-Controller", description = "북마크 관리")
@RestController
@RequestMapping("/api/bookmark")
@RequiredArgsConstructor
public class BookmarkController {

  private final BookmarkService bookmarkService;
  private final BookmarkMapper bookmarkMapper;

  @PostMapping("/check")
  public ResponseEntity<CreateBookmarkResponseDto> createBookmark(
      @RequestBody CreateBookmarkRequestDto dto,
      @AuthenticationPrincipal CustomUserDetails userDetails){
    CreateBookmarkResponseDto responseDto = bookmarkService.createBookmark(userDetails.getId(), dto);
    return  ResponseEntity.status(HttpStatus.CREATED).body(responseDto);

  }

  /**
   * {
   *   "page": 0,
   *   "size": 3,
   *   "sort": "createdDate"
   * }
   * */
  @GetMapping("/my-bookmarks")
  @Operation(summary = "내 북마크 목록 조회 (페이지네이션)", description = "로그인한 사용자가 북마크한 게시글 목록을 페이징하여 조회합니다.")
  public ResponseEntity<CustomPageResponseDto<MyBookmarkListResponseDto>> getMyBookmarks(
      @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    Page<BookmarkAndBoardDto> serviceResult = bookmarkService.getMyBookmarks(userDetails.getId(), pageable);

    List<MyBookmarkListResponseDto> webResponseContent = serviceResult.getContent().stream()
        .map(bookmarkMapper::toResponseDto)
        .collect(Collectors.toList());

    Page<MyBookmarkListResponseDto> responsePage = new PageImpl<>(
        webResponseContent,
        pageable,
        serviceResult.getTotalElements()
    );
    return ResponseEntity.ok(new CustomPageResponseDto<>(responsePage));
  }

}
