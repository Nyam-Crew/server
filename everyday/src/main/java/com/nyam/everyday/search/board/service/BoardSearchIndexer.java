package com.nyam.everyday.search.board.service;

import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.search.board.document.BoardDocument;
import jakarta.annotation.PreDestroy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;


/**
 * DB → Elasticsearch 색인(동기화) 진입점.
 * - create/update: index(...) (upsert 느낌, 덮어쓰기)
 * - delete: delete(id)
 * - flush(): 벌크 버퍼를 즉시 밀어넣기 (배치/종료 훅 등에서 호출)
 * 이번 단계에서는 "간단/안전"을 목표로 하고,
 * AFTER_COMMIT 이벤트/Outbox 등은 다음 단계에서 필요 시 확장한다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BoardSearchIndexer {

  private final ElasticsearchOperations operations;

  // 간단한 벌크 버퍼(트레픽 커지면 큐/스케쥴러 확장)
  private static final int BULK_THRESHOLD = 1;
  private final List<BoardDocument> buffer = new ArrayList<>();

  public static BoardDocument fromEntity(Board board) {
    String nickname = (board.getMember() != null ? board.getMember().getNickname() : null);

    return BoardDocument.builder()
        .boardId(board.getBoardId())
        .title(board.getBoardTitle())
        .content(board.getBoardContent())
        .nickname(nickname)
        .boardType(board.getBoardType())
        .createdDate(LocalDate.from(board.getCreatedDate()))
        .viewCount(board.getViewCount())
        .likeCount(board.getLikeCount())
        .commentCount(board.getCommentCount())
        .build();
  }
  public synchronized void index(Board board){
    if (board == null || board.getBoardId() == null) {return;}
    buffer.add(fromEntity(board));
  }

  /** 이미 만들어둔 문서를 직접 넣고 싶을 때(선택 API) */
  public synchronized void index(BoardDocument doc) {
    if (doc == null || doc.getBoardId() == null) return;
    buffer.add(doc);

  }
  /** 버퍼된 문서들을 벌크 저장 + 즉시 refresh(개발 단계에서는 편리) */
  public synchronized void flush() {
    if (buffer.isEmpty()) return;
    try {
      operations.save(buffer);                              // 벌크 색인
      operations.indexOps(BoardDocument.class).refresh();   // 개발/테스트: 즉시 반영
      log.debug("[ES] board bulk indexed: {}", buffer.size());
    } catch (Exception e) {
      log.error("[ES] board bulk indexing failed", e);
    } finally {
      buffer.clear();
    }
  }
  public void delete(Long boardId) {
    if (boardId == null) return;
    try {
      operations.delete(boardId.toString(),BoardDocument.class);
      operations.indexOps(BoardDocument.class).refresh();
    }catch (Exception e) {
      log.error("[ES] board bulk delete failed id ={}",boardId, e);
    }
  }
  @PreDestroy
  public void onShutdown() {
    flush(); // 서버 종료 시 잔여 버퍼 비우기
  }
}
