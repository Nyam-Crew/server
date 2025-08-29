package com.nyam.everyday.module.board.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.board.dto.BoardWithNicknameDto;
import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.module.board.repository.BoardRepository;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.entity.Status;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.search.board.document.BoardDocument;
import com.nyam.everyday.search.board.service.BoardSearchService;
import com.nyam.everyday.security.core.Role;
import com.nyam.everyday.web.board.dto.BoardDetailDto;
import com.nyam.everyday.web.board.dto.BoardPageDto;
import com.nyam.everyday.web.board.dto.BoardResponseDto;
import com.nyam.everyday.web.board.dto.CreateBoardRequestDto;
import com.nyam.everyday.web.board.dto.EditBoardRequestDto;
import com.nyam.everyday.web.board.mapper.BoardMapper;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

@Service
@RequiredArgsConstructor
public class BoardService {

  private final BoardRepository boardRepository;
  private final MemberRepository memberRepository;
  private final BoardMapper boardMapper;
  private final BoardSearchService boardSearchService;
  private final ApplicationEventPublisher eventPublisher;

  private String normalizeNonBlank(String raw){
    String trimmed = (raw == null) ? "" : raw.trim();
    if (trimmed.isEmpty()) throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
    return trimmed;
  }
  private String escapeLike(String s){
    return s.replace("!","!!")
        .replace("%","!%")
        .replace("_","!_");
  }
  private boolean isValidBoardType(String boardType) {
    return boardType.equals("recipe")
        || boardType.equals("before_after")
        || boardType.equals("freeTalk");
  }

  //게시글 생성 메서드
  @Transactional
  public BoardResponseDto createBoard(CreateBoardRequestDto dto, Long memberId) {

    //1. 작성자 조회
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

    if (member.getMemberStatus() == Status.DEACTIVATED){
      throw new BaseException(ErrorCode.MEMBER_DEACTIVATED);
    }

    Board board = boardMapper.toEntity(dto, member);
    Board saved = boardRepository.save(board);

    // 트랜잭션 커밋 후 색인
    eventPublisher.publishEvent(new BoardIndexedEvent(saved.getBoardId()));

    return boardMapper.toDto(saved);
  }

  //게시글 삭제
  @Transactional
  public void deleteBoard(Long boardId, Long memberId) {
    // 1.게시글 조회
    Board board = boardRepository.findById(boardId)
        .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));
    // 2.소유자 검증
    if (!board.getMember().getMemberId().equals(memberId)) {
      throw new BaseException(ErrorCode.ACCESS_DENIED);
    }

    //3 삭제
    boardRepository.delete(board);

    // 트랜잭션 커밋 후 ES에서도 삭제
    eventPublisher.publishEvent(new BoardDeletedEvent(boardId));
  }

  @Transactional(readOnly = true)
  public Page<BoardWithNicknameDto> getMyBoards(Long memberId, Pageable pageable) {
    return boardRepository.findByMemberId(memberId, pageable);
  }

  // 게시글 조회
  @Transactional
  public BoardDetailDto getBoard(Long boardId) {
    Board board = boardRepository.findWithMemberByBoardId(boardId)
        .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));
    board.increaseViewCount();
    Boolean likedByMe = null;
    return BoardDetailDto.from(board, likedByMe);
  }

  //게시글 수정
  @Transactional
  public BoardResponseDto editBoard(Long boardId, Long requesterId, EditBoardRequestDto dto){
    // 1.요청자 조회
    Member requester = memberRepository.findById(requesterId)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

    // 2.게시글 조회
    Board board = boardRepository.findById(boardId)
        .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

    // 3.작성자,관리자만 수정 가능
    boolean isAuthor = board.getMember().getMemberId().equals(requesterId);
    boolean isAdmin = requester.getRole() == Role.ROLE_ADMIN;
    if (!isAuthor && !isAdmin){
      throw new BaseException(ErrorCode.ACCESS_DENIED);
    }

    if (dto.getTitle() != null){
      String title = normalizeNonBlank(dto.getTitle());
      if (!title.equals(board.getBoardTitle())){
        board.setBoardTitle(title);
      }
    }
    if (dto.getContent() != null){
      String content = normalizeNonBlank(dto.getContent());
      if (!content.equals(board.getBoardContent())){
        board.setBoardContent(content);
      }
    }

    // 커밋 후 재색인
    eventPublisher.publishEvent(new BoardIndexedEvent(board.getBoardId()));
    return boardMapper.toDto(board);
  }

  // 게시글 미리보기
  @Transactional(readOnly = true)
  public Page<BoardPageDto> getBoardPreviews(String boardType, Pageable pageable) {
    if (boardType != null && !isValidBoardType(boardType)) {
      throw new BaseException(ErrorCode.INVALID_BOARD_TYPE);
    }
    return boardRepository.findBoardPreviews(boardType,pageable);
  }

  // 게시글 검색(제목/내용/닉네임/전체 등으로 검색)
  @Transactional(readOnly = true)
  public Page<BoardPageDto> searchBoards(
      String q, String boardType, Pageable pageable, String field
  ) {
    if (q == null || q.isBlank()){
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
    }
    String keyword = q.trim(); // ES analyzer가 처리
    String normType = (boardType == null || boardType.isBlank()) ? null : boardType.trim();
    String escaped = escapeLike(keyword);   // JPQL 대비 유물, ES에는 영향 없음

    String f = (field == null || field.isBlank()) ? "titlecontent" : field.trim();
    switch (f){
      case "title":
      case "content":
      case "nickname":
      case "titlecontent":
        break;
      default:
        throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
    }

    var esPage = boardSearchService.search(keyword, normType, f, pageable);

    List<Long> idsInOrder = esPage.getContent().stream()
        .map(BoardDocument::getBoardId)
        .filter(Objects::nonNull)
        .toList();
    if (idsInOrder.isEmpty()){
      return Page.empty(pageable);
    }

    List<Board> boards = boardRepository.findAllById(idsInOrder);
    Map<Long, Integer> order = new HashMap<>();
    for (int i = 0; i < idsInOrder.size(); i++) order.put(idsInOrder.get(i), i);
    boards.sort(Comparator.comparingInt(b -> order.getOrDefault(b.getBoardId(), Integer.MAX_VALUE)));

    List<BoardPageDto> content = boards.stream()
        .map(boardMapper::toPageDto)
        .toList();

    return new PageImpl<>(content, pageable, esPage.getTotalElements());
  }

  /** ==== 커밋 이후 ES 동기화를 위한 도메인 이벤트 ==== */
  @Getter
  public static class BoardIndexedEvent {
    private final Long boardId;
    public BoardIndexedEvent(Long boardId) { this.boardId = boardId; }
  }

  @Getter
  public static class BoardDeletedEvent {
    private final Long boardId;
    public BoardDeletedEvent(Long boardId) { this.boardId = boardId; }
  }
}