package com.nyam.everyday.search.board.repository;

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.nyam.everyday.search.board.document.BoardDocument;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BoardSearchRepositoryImpl implements BoardSearchRepositoryCustom {

  private final ElasticsearchOperations elasticsearchOperations;

  @Override
  public Page<BoardDocument> search(String keyword,
      String boardType,
      boolean inTitle,
      boolean inContent,
      boolean inNick,
      Pageable pageable) {

    /*
     [파트 1] 검색 대상 필드 구성 (+가중치)
      - flags(inTitle/inContent/inNick)에 따라 필드 목록을 만든다.
      - .ngram 서브필드는 짧은 입력(접두 검색)에 유리하므로 약간의 boost를 준다.
      - 비어있는 경우(title 기본) 안전장치 포함.
      - 본문보다 제목에 가중치를 더 준 이유는 본문은 내용이 길어 유사도가 왜곡될 수 있기 때문.
     */
    List<String> fields = new ArrayList<>();
    if (inTitle) {
      fields.add("title^2");          // 제목 가중치
      fields.add("title.ngram^1.5");  // 접두 일치 강화
    }
    if (inContent) {
      fields.add("content");
      fields.add("content.ngram");
    }
    if (inNick) {
      fields.add("nickname.search");
      fields.add("nickname.ngram^1.2");
    }
    if (fields.isEmpty()) { // 기본: 제목 검색
      fields.add("title^2");
      fields.add("title.ngram^1.5");
    }

    /*
     [파트 2] 쿼리 빌드 (NativeQuery + Bool)
      - 키워드가 있으면 SHOULD에 (1) multi_match, (2) title.keyword wildcard 폴백을 추가
      - 최소 1개 should 매칭 필요(minimum_should_match = 1)
      - boardType이 있으면 term FILTER
      - Pageable로 페이징/정렬 전달
     */
    NativeQuery query = NativeQuery.builder()
        .withQuery(q -> q.bool(b -> {
          // 키워드 처리
          if (keyword != null && !keyword.isBlank()) {
            // (1) multi_match
            b.should(s -> s.multiMatch(
                mm -> mm
                    .query(keyword)
                    .fields(fields)
                    .type(TextQueryType.BestFields)
            ));

            // (2) 폴백: 제목 스코프가 활성화된 경우 title.keyword 와일드카드
            if (inTitle) {
              String wc = escapeForWildcard(keyword);
              b.should(s -> s.wildcard(w -> w
                  .field("title.keyword")
                  .wildcard("*" + wc + "*")
              ));
            }

            // 최소 한 개는 매칭되도록
            b.minimumShouldMatch("1");
          }

          // 타입 필터 (keyword 필드 → term)
          if (boardType != null && !boardType.isBlank()) {
            b.filter(f -> f.term(t -> t.field("boardType").value(boardType)));
          }

          return b;
        }))
        .withPageable(pageable)
        .build();

    /*
     [파트 3] 실행 및 결과 매핑
      - ES 검색 실행 → SearchHits → Page<BoardDocument> 변환
     */
    SearchHits<BoardDocument> hits = elasticsearchOperations.search(query, BoardDocument.class);
    return SearchHitSupport.searchPageFor(hits, pageable)
        .map(SearchHit::getContent);
  }

  /**
   * ES wildcard 쿼리용 이스케이프
   *  - \, ?, * 는 리터럴로 검색되도록 escape
   */
  private static String escapeForWildcard(String s) {
    if (s == null || s.isEmpty()) return s;
    return s
        .replace("\\", "\\\\")
        .replace("?", "\\?")
        .replace("*", "\\*");
  }
}
