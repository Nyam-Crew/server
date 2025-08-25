package com.nyam.everyday.search.board.service;

import com.nyam.everyday.search.board.document.BoardDocument;
import com.nyam.everyday.search.board.repository.BoardSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Board 검색 전용 서비스 레이어.
 *  - 컨트롤러/도메인 서비스에서 넘어온 파라미터를 정리하고
 *  - Repository(Custom Impl)로 실제 ES 쿼리를 위임한다.
 *  - 검색 스코프(제목/내용/닉네임) 편의 오버로드 제공.
 *  - 빈/공백 키워드 방어, 페이징 안전장치 등 UX/성능 배려.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BoardSearchService {

  // Elasticsearch Repository 주입 (Custom 구현 포함)
  private final BoardSearchRepository boardSearchRepository;

  /**
   * [편의] 기본 스코프(제목+내용)로 검색.
   *  - UI가 별도 스코프를 주지 않을 때 사용.
   */
  @Transactional(readOnly = true)
  public Page<BoardDocument> search(String keyword, String boardType, Pageable pageable) {
    // 기본 스코프: 제목+내용 (닉네임 제외)
    return search(keyword, boardType, true, true, false, pageable);
  }

  /**
   * [편의] 스코프 문자열(field)로 검색.
   *  - "title", "content", "nickname", "titlecontent" 를 지원.
   *  - 그 외 값은 titlecontent로 폴백.
   */
  @Transactional(readOnly = true)
  public Page<BoardDocument> search(String keyword, String boardType, String field, Pageable pageable) {
    Scope scope = Scope.from(field);
    return search(keyword, boardType, scope.inTitle, scope.inContent, scope.inNick, pageable);
  }

  /**
   * [기본] 스코프 플래그로 검색 수행.
   *  - 빈/공백 키워드는 빈 페이지 반환(무의미한 match_all 방지)
   *  - boardType은 null/빈문자열이면 무시 (필터 미적용)
   *  - 나머지는 Repository(Custom Impl)로 위임
   */
  @Transactional(readOnly = true)
  public Page<BoardDocument> search(String keyword,
                                    String boardType,
                                    boolean inTitle,
                                    boolean inContent,
                                    boolean inNick,
                                    Pageable pageable) {
    String q = normalize(keyword);
    if (q == null) {
      // 검색어가 없으면 빈 결과를 돌려 안정적으로 처리 (리스트 페이지에서 match_all 방지)
      return Page.empty(pageable);
    }
    String type = normalize(boardType); // null/빈문자열이면 필터 미적용

    // 실제 ES 쿼리는 Custom Repository에서 수행 (ngram + 형태소 + 필터/정렬)
    return boardSearchRepository.search(q, type, inTitle, inContent, inNick, pageable);
  }

  /** 저장/삭제 편의 메서드 (색인 동기화 시 사용) */
  @Transactional
  public void save(BoardDocument doc) { boardSearchRepository.save(doc); }

  @Transactional
  public void deleteById(Long boardId) { boardSearchRepository.deleteById(boardId); }


  /** 공백/탭만 있는 문자열은 null로 정규화 */
  private static String normalize(String s) {
    if (s == null) return null;
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }

  /** 검색 스코프 표현 (문자열 → 플래그 매핑) */
  private enum Scope {
    TITLE       (true,  false, false),
    CONTENT     (false, true,  false),
    NICKNAME    (false, false, true),
    TITLECONTENT(true,  true,  false);

    final boolean inTitle;
    final boolean inContent;
    final boolean inNick;

    Scope(boolean t, boolean c, boolean n) {
      this.inTitle = t; this.inContent = c; this.inNick = n;
    }

    static Scope from(String field) {
      if (field == null || field.isBlank()) return TITLECONTENT;
      switch (field.trim().toLowerCase()) {
        case "title":        return TITLE;
        case "content":      return CONTENT;
        case "nickname":     return NICKNAME;
        case "titlecontent": return TITLECONTENT;
        default:               return TITLECONTENT; // 안전 폴백
      }
    }
  }
}
