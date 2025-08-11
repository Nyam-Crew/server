package com.nyam.everyday.module.board.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.module.board.repository.BoardRepository;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.entity.Status;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.web.board.dto.BoardResponseDto;
import com.nyam.everyday.web.board.dto.CreateBoardRequestDto;
import com.nyam.everyday.web.board.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

  private final BoardRepository boardRepository;
  private final MemberRepository memberRepository;
  private final BoardMapper boardMapper;

  //게시글 생성 메서드
  @Transactional
  public BoardResponseDto createBoard(CreateBoardRequestDto dto,Long memberId) {

    //1. 작성자 조회
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

    if (member.getMemberStatus() == Status.DEACTIVATED){
      throw new BaseException(ErrorCode.MEMBER_DEACTIVATED);
    }

    Board board = boardMapper.toEntity(dto, member);
    Board saved =  boardRepository.save(board);
    return boardMapper.toDto(saved);





  }
  @Transactional
  public void deleteBoard(Long boardId, Long memberId) {
    // 1.게시글 조회 시
    Board board = boardRepository.findById(boardId)
        .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));
  }

}
