package com.nyam.everyday.search.board.repository;

import com.nyam.everyday.search.board.document.BoardDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardSearchRepositoryCustom {
  Page<BoardDocument> search(String keyword, String boardType,
      boolean inTitle, boolean inContent, boolean inNick,
      Pageable pageable);

}
