package com.nyam.everyday.module.boardLike.service;


// import co.elastic.clients.elasticsearch._types.query_dsl.Like;
// import com.fasterxml.jackson.databind.ser.Serializers.Base;
import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.module.board.repository.BoardRepository;
import com.nyam.everyday.module.boardLike.entity.BoardLike;
import com.nyam.everyday.module.boardLike.repository.BoardLikeRepository;
import com.nyam.everyday.module.challenge.checker.event.event.ChallengeCheckEvent;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.web.boardlike.dto.BoardLikeResponseDto;
import com.nyam.everyday.web.boardlike.dto.LikeStatusResponseDto;
import com.nyam.everyday.web.boardlike.dto.ToggleLikeResponseDto;
// import com.nyam.everyday.web.boardlike.mapper.BoardLikeMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardLikeService {

  private final BoardLikeRepository boardLikeRepository;
  private final BoardRepository boardRepository;
  private final MemberRepository memberRepository;
  private final ApplicationEventPublisher publisher;



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
      BoardLike saved = BoardLike.builder()
          .member(member)
          .board(board)
          .build();
      boardLikeRepository.save(saved);
      likeId = saved.getBoardLikeId();
      liked = true;

      // 생성 시에만 이벤트 발행
      publisher.publishEvent(new ChallengeCheckEvent(memberId, ChallengeTag.LIKE, LocalDate.now()));
    }

    long likeCount = boardLikeRepository.countByBoard(board);
    board.updateLikeCount(likeCount);

    return BoardLikeResponseDto.builder()
        .likeId(likeId)          // 생성된 경우만 값이 들어감, 취소면 null
        .memberId(memberId)
        .boardId(boardId)
        .liked(liked)            //  토글 후 상태
        .likeCount(likeCount)
        .build();
  }



  @Transactional
  public ToggleLikeResponseDto toggle(Long boardId, Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));
    Board board = boardRepository.findById(boardId)
        .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

    boolean wasLiked = boardLikeRepository.existsByMemberAndBoard(member, board);

    if (wasLiked) {
      boardLikeRepository.deleteByMemberAndBoard(member, board);
    } else {
      BoardLike like = BoardLike.builder()
          .member(member)
          .board(board)
          .build();
      boardLikeRepository.save(like);

      // 생성 시에만 이벤트 발행
      publisher.publishEvent(new ChallengeCheckEvent(memberId, ChallengeTag.LIKE, LocalDate.now()));
    }

    long total = boardLikeRepository.countByBoard(board);
    board.updateLikeCount(total);

    boolean nowLiked = !wasLiked;            // 토글 후 상태
    return new ToggleLikeResponseDto(total, nowLiked); // { totalLikes, isLiked }
  }
  @Transactional
  public LikeStatusResponseDto status(List<Long> boardIds, Long memberId) {
    if (boardIds == null || boardIds.isEmpty()) {
      return new LikeStatusResponseDto(Collections.emptyList());
    }
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));
    List<Board> boards = boardRepository.findAllById(boardIds);

    var liked = boardLikeRepository.findAllByMemberAndBoardIn(member, boards)
        .stream()
        .map(b1 -> b1.getBoard().getBoardId())
        .toList();
    return new LikeStatusResponseDto(liked);
  }



}
