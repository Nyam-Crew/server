package com.nyam.everyday.search.board.repository;

import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.search.board.document.BoardDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BoardSearchRepository extends ElasticsearchRepository<BoardDocument, Long>, BoardSearchRepositoryCustom{

}
