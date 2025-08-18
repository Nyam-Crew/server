package com.nyam.everyday.module.boardLike.service;


import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.module.board.repository.BoardRepository;
import com.nyam.everyday.module.boardLike.entity.BoardLike;
import com.nyam.everyday.module.boardLike.repository.BoardLikeRepository;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.web.boardlike.dto.BoardLikeResponseDto;
import com.nyam.everyday.web.boardlike.mapper.BoardLikeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardLikeService {

  private final BoardLikeRepository boardLikeRepository;
  private final BoardRepository boardRepository;
  private final MemberRepository memberRepository;
  private final BoardLikeMapper boardLikeMapper;


  //토글 메서드로 진행한 좋아요
  @Transactional
  public BoardLikeResponseDto toggleBoardLike(Long boardId, Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));
    Board board = boardRepository.findById(boardId)
        .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

    Long likeId = null;
    boolean liked;

    if (boardLikeRepository.existsByMemberAndBoard(member, board)) {
      // 이미 눌러져 있으면 삭제
      boardLikeRepository.deleteByMemberAndBoard(member, board);
      liked = false;
    } else {
      // 없으면 생성
      BoardLike saved = boardLikeRepository.save(boardLikeMapper.toEntity(board, member));
      likeId = saved.getBoardLikeId();   // ← 여기서 PK 설정
      liked = true;
    }

    long likeCount = boardLikeRepository.countByBoard(board);
    board.updateLikeCount(likeCount); // or setLikeCount

    return BoardLikeResponseDto.builder()
        .likeId(likeId)          // ← 생성된 경우만 값이 들어감, 취소면 null
        .memberId(memberId)
        .boardId(boardId)
        .liked(liked)
        .likeCount(likeCount)
        .build();
  }

  //좋아요 적용 취소 따로 적용한 메서드
//  // 1.좋아요 등록
//  public BoardLikeResponseDto createBoardLike(Long boardId, Long memberId) {
//    Member member  = memberRepository.findById(memberId)
//        .orElseThrow(()-> new BaseException(ErrorCode.MEMBER_NOT_FOUND));
//
//    Board board = boardRepository.findById(boardId)
//        .orElseThrow(()-> new BaseException(ErrorCode.BOARD_NOT_FOUND));
//
//    if (boardLikeRepository.existsByMemberIdAndBoardId(member, board)) {
//      throw new BaseException(ErrorCode.ALREADY_LIKED);
//    }
//
//    // 2.엔티티 생성(Mapper default 메서드 반영)
//    BoardLike saved = boardLikeRepository.save(
//        boardLikeMapper.toEntity(board, member)
//    );
//    // 3.카운트 반영
//    long likeCount = boardLikeRepository.countByBoardId(board);
//
//    return BoardLikeResponseDto.builder()
//        .likeId(saved.getBoardLikeId())
//        .boardId(board.getBoardId())
//        .memberId(member.getMemberId())
//        .liked(true)
//        .likeCount(likeCount)
//        .build();
//  }
//  // 좋아요 취소
//  public BoardLikeResponseDto deleteBoardLike(Long boardId, Long memberId) {
//
//
//    Member member = memberRepository.findById(memberId)
//        .orElseThrow(()-> new BaseException(ErrorCode.MEMBER_NOT_FOUND));
//
//    Board board = boardRepository.findById(boardId)
//        .orElseThrow(()-> new BaseException(ErrorCode.BOARD_NOT_FOUND));
//
//    boardLikeRepository.deleteByMemberIdAndBoardId(member, board);
//
//    long likeCount = boardLikeRepository.countByBoardId(board);
//    return BoardLikeResponseDto.builder()
//        .boardId(boardId)
//        .memberId(memberId)
//        .liked(false)
//        .likeCount(likeCount)
//        .build();
//
//  }



}
