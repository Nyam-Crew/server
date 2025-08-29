package com.nyam.everyday.search.board.handler;

import com.nyam.everyday.module.board.repository.BoardRepository;
import com.nyam.everyday.module.board.service.BoardService.BoardDeletedEvent;
import com.nyam.everyday.module.board.service.BoardService.BoardIndexedEvent;
import com.nyam.everyday.search.board.service.BoardSearchIndexer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
@Slf4j
public class BoardIndexEventHandler {

  private final BoardRepository boardRepository;
  private final BoardSearchIndexer boardSearchIndexer;

  /** 생성/수정 → 커밋 후 색인 */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onIndexed(BoardIndexedEvent event) {
    Long id = event.getBoardId();
    boardRepository.findById(id).ifPresent(board -> {
      try {
        boardSearchIndexer.index(board);
        log.info("[ES] indexed boardId={}", id);
      } catch (Exception e) {
        log.error("[ES] index failed boardId=" + id, e);
      }
    });
  }

  /** 삭제 → 커밋 후 ES 문서 삭제 */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onDeleted(BoardDeletedEvent event) {
    Long id = event.getBoardId();
    try {
      boardSearchIndexer.delete(id);
      log.info("[ES] deleted boardId={}", id);
    } catch (Exception e) {
      log.error("[ES] delete failed boardId=" + id, e);
    }
  }
}