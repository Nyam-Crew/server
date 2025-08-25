package com.nyam.everyday.module.boardComment.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.module.board.repository.BoardRepository;
import com.nyam.everyday.module.boardComment.entity.BoardComment;
import com.nyam.everyday.module.boardComment.repository.BoardCommentRepository;
import com.nyam.everyday.module.challenge.checker.event.event.ChallengeCheckEvent;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.entity.Status;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.security.core.Role;
import com.nyam.everyday.web.boardComment.dto.CommentResponseDto;
import com.nyam.everyday.web.boardComment.dto.CreateCommentRequestDto;
import com.nyam.everyday.web.boardComment.mapper.BoardCommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardCommentService {

  private final BoardRepository boardRepository;
  private final MemberRepository memberRepository;
  private final BoardCommentRepository boardCommentRepository;
  private final BoardCommentMapper boardCommentMapper;
  private final ApplicationEventPublisher publisher;

  private static final int MAX_CONTENT_LENGTH = 2_000;
  private static final String SOFT_DELETED_MESSAGE = "삭제된 댓글입니다";

  private boolean isAuthor(BoardComment comment, Member requester) {
    return comment.getMember().getMemberId().equals(requester.getMemberId());
  }

  private boolean isAdmin(Long requesterId) {
    return memberRepository.findById(requesterId)
        .map(r -> r.getRole() == Role.ROLE_ADMIN)
        .orElse(false);
  }

  private String validate(String raw) {
    if (raw == null) throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
    String trimmed = raw.trim();
    if (trimmed.isEmpty()) throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
    if (trimmed.length() > MAX_CONTENT_LENGTH) throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
    return HtmlUtils.htmlEscape(trimmed);
  }

  private boolean isSoftDeleted(BoardComment c) {
    return SOFT_DELETED_MESSAGE.equals(c.getContent());
  }

  // =========================
  // 댓글 생성 (루트/대댓글 공통)
  // =========================
  @Transactional
  public CommentResponseDto createComment(Long boardId, CreateCommentRequestDto dto, Long memberId) {

    if (dto == null || dto.getContent() == null || dto.getContent().isBlank()) {
      throw new BaseException(ErrorCode.INVALID_REQUEST);
    }

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));
    if (member.getMemberStatus() == Status.DEACTIVATED) {
      throw new BaseException(ErrorCode.MEMBER_DEACTIVATED);
    }

    Board board = boardRepository.findById(boardId)
        .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

    String normalized = validate(dto.getContent());

    BoardComment comment = boardCommentMapper.toEntity(dto, board, member);
    comment.editContent(normalized);

    // 대댓글이면 부모 검증
    if (dto.getParentId() != null) {
      BoardComment parent = boardCommentRepository.findById(dto.getParentId())
          .orElseThrow(() -> new BaseException(ErrorCode.COMMENT_NOT_FOUND));
      if (!parent.getBoard().getBoardId().equals(board.getBoardId())) {
        throw new BaseException(ErrorCode.INVALID_REQUEST);
      }
      comment.setParent(parent);
    }

    BoardComment saved = boardCommentRepository.save(comment);
    board.increaseCommentCount();

    // 이벤트 발급
    publisher.publishEvent(new ChallengeCheckEvent(memberId, ChallengeTag.COMMENT, LocalDate.now()));

    return boardCommentMapper.toResponseDto(saved);
  }

  // =========================
  // 루트 댓글 페이징 (parent is null)
  //  + childCount 주입
  // =========================
  @Transactional(readOnly = true)
  public Page<CommentResponseDto> getRootCommentsPage(Long boardId, Pageable pageable) {
    if (boardId == null || pageable == null) {
      throw new BaseException(ErrorCode.INVALID_REQUEST);
    }
    Page<BoardComment> page = boardCommentRepository
        .findPageByBoardIdAndParentIsNull(boardId, pageable);

    Page<CommentResponseDto> dtoPage = page.map(boardCommentMapper::toResponseDto);

    // childCount 주입
    List<CommentResponseDto> contentWithCounts = enrichWithChildCount(dtoPage.getContent());
    return dtoPage.map(dto -> contentWithCounts.stream()
        .filter(x -> x.getCommentId().equals(dto.getCommentId()))
        .findFirst()
        .orElse(dto));
  }

  // =========================
  // 특정 댓글의 대댓글 페이징 (parent = :parentId)
  // =========================
  @Transactional(readOnly = true)
  public Page<CommentResponseDto> getRepliesPage(Long boardId, Long parentId, Pageable pageable) {
    if (boardId == null || parentId == null || pageable == null) {
      throw new BaseException(ErrorCode.INVALID_REQUEST);
    }
    Page<BoardComment> page = boardCommentRepository
        .findPageByBoardIdAndParentId(boardId, parentId, pageable);
    // 대댓글에는 childCount가 의미 없으므로 그대로 반환
    return page.map(boardCommentMapper::toResponseDto);
  }

  // =========================
  // 댓글 삭제
  // =========================
  @Transactional
  public void deleteComment(Long commentId, Long requesterId) {
    BoardComment comment = boardCommentRepository.findByIdWithMemberAndBoard(commentId)
        .orElseThrow(() -> new BaseException(ErrorCode.COMMENT_NOT_FOUND));

    Board board = comment.getBoard();
    boolean isAuthor = comment.getMember().getMemberId().equals(requesterId);
    boolean isBoardOwner = board.getMember().getMemberId().equals(requesterId);
    boolean isAdmin = isAdmin(requesterId);

    if (!(isAuthor || isBoardOwner || isAdmin)) {
      throw new BaseException(ErrorCode.ACCESS_DENIED);
    }

    boolean hasChild = boardCommentRepository.existsByParent(comment);

    if (hasChild) {
      comment.markDeleted(); // 소프트 삭제
    } else {
      boardCommentRepository.delete(comment); // 하드 삭제
      board.decreaseCommentCount();
    }

    // 이벤트 발급
    publisher.publishEvent(new ChallengeCheckEvent(comment.getMember().getMemberId(), ChallengeTag.COMMENT, LocalDate.now()));
  }

  // =========================
  // 댓글 수정
  // =========================
  @Transactional
  public CommentResponseDto editComment(Long commentId, Long requesterId, String edit) {
    BoardComment comment = boardCommentRepository.findByIdWithMemberAndBoard(commentId)
        .orElseThrow(() -> new BaseException(ErrorCode.COMMENT_NOT_FOUND));

    Member requester = memberRepository.findById(requesterId)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

    if (!isAuthor(comment, requester)) {
      throw new BaseException(ErrorCode.ACCESS_DENIED);
    }
    if (isSoftDeleted(comment)) {
      throw new BaseException(ErrorCode.COMMENT_ALREADY_DELETED);
    }

    String normalized = validate(edit);
    if (normalized.equals(comment.getContent())) {
      return boardCommentMapper.toResponseDto(comment);
    }

    comment.editContent(normalized);
    return boardCommentMapper.toResponseDto(comment);
  }

  // =========================
  // 내부 유틸: childCount 주입
  // =========================
  private List<CommentResponseDto> enrichWithChildCount(List<CommentResponseDto> roots) {
    if (roots == null || roots.isEmpty()) return roots;

    Set<Long> parentIds = roots.stream()
        .map(CommentResponseDto::getCommentId)
        .collect(Collectors.toSet());

    Map<Long, Long> countMap = boardCommentRepository.countChildrenByParentIds(parentIds).stream()
        .collect(Collectors.toMap(BoardCommentRepository.ParentCount::getParentId,
            BoardCommentRepository.ParentCount::getCnt));

    // childCount를 세팅한 새 DTO로 치환
    return roots.stream()
        .map(r -> CommentResponseDto.builder()
            .commentId(r.getCommentId())
            .boardId(r.getBoardId())
            .memberId(r.getMemberId())
            .content(r.getContent())
            .nickname(r.getNickname())
            .parentId(r.getParentId())
            .createdDate(r.getCreatedDate())
            .modifiedDate(r.getModifiedDate())
            .edited(r.isEdited())
            .childCount(countMap.getOrDefault(r.getCommentId(), 0L)) // 핵심
            .build())
        .toList();
  }
}