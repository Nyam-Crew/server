package com.nyam.everyday.module.boardComment.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.module.board.repository.BoardRepository;
import com.nyam.everyday.module.boardComment.entity.BoardComment;
import com.nyam.everyday.module.boardComment.repository.BoardCommentRepository;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.entity.Status;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.web.boardComment.dto.CreateCommentRequestDto;
import com.nyam.everyday.web.boardComment.dto.CreateCommentResponseDto;
import com.nyam.everyday.web.boardComment.mapper.BoardCommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardCommentService {

  private final BoardRepository boardRepository;
  private final MemberRepository memberRepository;
  private final BoardCommentRepository boardCommentRepository;
  private final BoardCommentMapper boardCommentMapper;


  //댓글 생성/댓글과 대댓글 구분
  @Transactional
  public CreateCommentResponseDto createComment(Long boardId,CreateCommentRequestDto dto, Long memberId) {

    if (dto == null || dto.getContent() == null || dto.getContent().isBlank()) {
      throw new BaseException(ErrorCode.INVALID_REQUEST);
    }

    // 1.사용자 조회 및 상태 검증
    Member member = memberRepository.findById(memberId)
    .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

    if (member.getMemberStatus() == Status.DEACTIVATED){
      throw new BaseException(ErrorCode.MEMBER_DEACTIVATED);
    }
    // 2.게시글 존재 여부 확인
    Board board = boardRepository.findById(boardId)
    .orElseThrow(()-> new BaseException(ErrorCode.BOARD_NOT_FOUND));

    // 3.댓글 엔티티 생성 및 저장
    BoardComment comment = boardCommentMapper.toEntity(dto, board, member);

    // 4. 대댓글 조건 처리: parentId가 있으면 부모 조히/검증 후 연결
    if (dto.getParentId() != null) {
      BoardComment parent = boardCommentRepository.findById(dto.getParentId())
          .orElseThrow(() -> new BaseException(ErrorCode.COMMENT_NOT_FOUND));

      if (!parent.getBoard().getBoardId().equals(board.getBoardId())){
        throw new BaseException(ErrorCode.INVALID_REQUEST);
      }
      comment.setParent(parent);
    }
    BoardComment saved = boardCommentRepository.save(comment);



    // 4.게시글의 댓글 수 증가
    board.increaseCommentCount();

    return boardCommentMapper.toResponseDto(saved);
  }

  /**
   * [댓글/대댓글 페이지 조회]
   * - 입력: 게시글 ID, Pageable(page/size/sort)
   * - 처리: N+1 방지용 fetch join 쿼리로 Page<BoardComment> 조회
   * - 출력: Page<CreateCommentResponseDto> (commentId, parentId, nickname, content, createdDate …)
   */
  public Page<CreateCommentResponseDto> getCommentsPage(Long boardId, Pageable pageable) {

    // 1. 필수 파라미터 가드 (빠른 실패)
    if (boardId == null) {
      throw new BaseException(ErrorCode.INVALID_REQUEST);
    }
    if (pageable == null) {
      throw new BaseException(ErrorCode.INVALID_REQUEST);
    }

    // 2.레포 호출(fetch join + countQuery 분리) -> N + 1 방지된 페이지 결과
    Page<BoardComment> page = boardCommentRepository
        .findPageByBoardIdWithJoins(boardId, pageable);

    // 3.엔티티 페이지 -> DTO 페이지 매핑 (MapStruct 사용)
    // 순환참조 없이 필요한 값만 노출 (닉네임, parentId)
    return page.map(boardCommentMapper::toResponseDto);
  }


}
