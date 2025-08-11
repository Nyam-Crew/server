package com.nyam.everyday.web.bookmark.controller;



import com.nyam.everyday.module.bookmark.service.BookmarkService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.bookmark.dto.CreateBookmarkRequestDto;
import com.nyam.everyday.web.bookmark.dto.CreateBookmarkResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookmark")
@RequiredArgsConstructor
public class BookmarkController {

  private final BookmarkService bookmarkService;

  @PostMapping("/check")
  public ResponseEntity<CreateBookmarkResponseDto> createBookmark(
      @RequestBody CreateBookmarkRequestDto dto,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ){
    CreateBookmarkResponseDto responseDto = bookmarkService.createBookmark(userDetails.getId(), dto);
    return  ResponseEntity.status(HttpStatus.CREATED).body(responseDto);

  }

}
