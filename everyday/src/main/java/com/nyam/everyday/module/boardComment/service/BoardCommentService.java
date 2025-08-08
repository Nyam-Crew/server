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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardCommentService {

  private final BoardRepository boardRepository;
  private final MemberRepository memberRepository;
  private final BoardCommentRepository boardCommentRepository;
  private final BoardCommentMapper boardCommentMapper;

  @Transactional
  public CreateCommentResponseDto createComment(Long boardId,CreateCommentRequestDto dto, Long memberId) {
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
    boardCommentRepository.save(comment);



    // 4.게시글의 댓글 수 증가
    board.increaseCommentCount();
    boardRepository.save(board);//변경사항 반영

    return boardCommentMapper.toResponseDto(comment);
  }

}
