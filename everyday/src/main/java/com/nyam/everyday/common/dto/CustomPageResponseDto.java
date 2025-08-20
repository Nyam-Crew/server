package com.nyam.everyday.common.dto;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@Getter
public class CustomPageResponseDto<T> {

  private final List<T> content;      // 데이터 목록
  private final int pageNumber;       // 현재 페이지 번호 (0부터 시작)
  private final int pageSize;         // 페이지 크기
  private final int totalPages;       // 전체 페이지 수
  private final long totalElements;   // 전체 요소 수
  private final boolean isLast;       // 마지막 페이지 여부

  public CustomPageResponseDto(Page<T> page) {
    this.content = page.getContent();
    this.pageNumber = page.getNumber();
    this.pageSize = page.getSize();
    this.totalPages = page.getTotalPages();
    this.totalElements = page.getTotalElements();
    this.isLast = page.isLast();
  }

  public <U> CustomPageResponseDto<U> map(Function<? super T, ? extends U> converter) {
    List<U> convertedContent = this.content.stream()
        .map(converter)
        .collect(Collectors.toList());

    Page<U> newPage = new PageImpl<>(
        convertedContent,
        PageRequest.of(this.pageNumber, this.pageSize),
        this.totalElements
    );

    return new CustomPageResponseDto<>(newPage);
  }
}
