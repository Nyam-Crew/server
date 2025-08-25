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
  public Page<BoardDocument> search(String keyword, String boardType, boolean inTitle,
      boolean inContent, boolean inNick, Pageable pageable) {

    /*
     [파트 1] 검색 대상 필드 구성 (+가중치)
     - flags(inTitle/inContent/inNick)에 따라 필드 목록을 만든다.
     - .ngram 서브필드는 짧은 입력(접두 검색)에 유리하므로 약간의 boost를 준다.
     - 비어있는 경우(title 기본) 안전장치 포함.
     본문보다 제목에 가중치를 더 준 이유는 본문은 내용이 길어 유사도에 걸리는 내용마다 의미가 다른 글일 경우가 있을 수도 있기 때문에 content는 가중치 미설정
     */
    List<String> fields = new ArrayList<>();
    if (inTitle) {
      fields.add("title^2");          //제목 가중치
      fields.add("title.ngram^1.5");  //접두 일치 강화
    }
    if (inContent) {
      fields.add("content");
      fields.add("content.ngram");
    }
    if (inNick) {
      fields.add("nickname.search");
      fields.add("nickname.ngram^1.2");
    }
    if (fields.isEmpty()) {             // 기본: 제목 검색
      fields.add("title");
      fields.add("title.ngram");
    }
    // [파트 2] 쿼리 빌드 (NativeQuery + Bool)
    // - 키워드가 있으면 multi_match MUST
    // - boardType이 있으면 term FILTER
    // - Pageable로 페이징/정렬 전달
    NativeQuery query = NativeQuery.builder()
        .withQuery(q -> q.bool(b -> { // bool: must(점수 반영) + filter(점수 비반영)
          if (keyword != null && !keyword.isBlank()) {
            b.must(m -> m.multiMatch(
                mm -> mm
                    .query(keyword)
                    .fields(fields)
                    .type(TextQueryType.BestFields) // BEST_FIELDS: 필드 중 가장 관련 높은 걸 우선
            )); // 키워드가 있으면 multi_match로 점수 계산
          }
          if (boardType != null && !boardType.isBlank()) {
            b.filter(f -> f.term(t -> t.field("boardType").value(boardType))); // boardType은 필터(점수 무관, 캐시 가능)
          }
          return b;
        }))
        .withPageable(pageable) // Spring Pageable → from/size/sort 전달
        .build();
    // [파트 3] 실행 및 결과 매핑
    // - ES 검색 실행 → SearchHits → Page<BoardDocument> 변환
    SearchHits<BoardDocument> hits = elasticsearchOperations.search(query, BoardDocument.class); // ES 쿼리 실행
    return SearchHitSupport.searchPageFor(hits, pageable) // SearchHits → Page 변환
        .map(SearchHit::getContent);                       // Page<SearchHit> → Page<Doc>

  }
}
