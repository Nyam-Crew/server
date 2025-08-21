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
import com.nyam.everyday.security.core.Role;
import com.nyam.everyday.web.boardComment.dto.CommentResponseDto;
import com.nyam.everyday.web.boardComment.dto.CreateCommentRequestDto;
import com.nyam.everyday.web.boardComment.mapper.BoardCommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

@Service
@RequiredArgsConstructor
public class BoardCommentService {

  private final BoardRepository boardRepository;
  private final MemberRepository memberRepository;
  private final BoardCommentRepository boardCommentRepository;
  private final BoardCommentMapper boardCommentMapper;

  private static final int MAX_CONTENT_LENGTH = 2_000;
  private static final String SOFT_DELETED_MESSAGE = "삭제된 댓글입니다";


  private boolean isAuthor(BoardComment comment, Member requester) {
    return comment.getMember().getMemberId().equals(requester.getMemberId());
  }

  private boolean isAdmin(Long requesterId) {
    return memberRepository.findById(requesterId)
        .map(r ->r.getRole() == Role.ROLE_ADMIN)
        .orElse(false);
  }
  private String validate(String raw){
    if (raw == null) throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
    String trimmed = raw.trim();
    if (trimmed.isEmpty()) throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
    if (trimmed.length() > MAX_CONTENT_LENGTH) throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
    return HtmlUtils.htmlEscape(trimmed);


  }

  private boolean isSoftDeleted(BoardComment c){
    return SOFT_DELETED_MESSAGE.equals(c.getContent());
  }

  //댓글 생성/댓글과 대댓글 구분
  @Transactional
  public CommentResponseDto createComment(Long boardId,CreateCommentRequestDto dto, Long memberId) {

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

    //내용 정규화: 공백/길이/XSS 검사 + escape
    String normalized = validate(dto.getContent());

    // 3.댓글 엔티티 생성 및 저장
    BoardComment comment = boardCommentMapper.toEntity(dto, board, member);

    //안전한 내용으로 교체(더티체킹 트리거)
    comment.editContent(normalized);
    // 4. 대댓글 조건 처리: parentId가 있으면 부모 조회/검증 후 연결
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
   * - 출력: Page<CommentResponseDto> (commentId, parentId, nickname, content, createdDate …)
   */
  @Transactional(readOnly = true)
  public Page<CommentResponseDto> getCommentsPage(Long boardId, Pageable pageable) {

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

  /**
   * [댓글/대댓글 삭제]
   * - 권한: 댓글 작성자 본인, 게시글 작성자, 관리자(ROLE_ADMIN)만 허용
   * - 자식(대댓글) 존재 시: 소프트 삭제(내용 마스킹) — 트리 보존, count 유지
   * - 자식 없음: 하드 삭제(레코드 제거) — commentCount 감소(증감식 정책)
   */
  @Transactional
  public void deleteComment(Long commentId,Long requesterId) {

    // 1.대상 댓글 로드(작성자/게시글까지 fetch join으로 한 번에)
    BoardComment comment = boardCommentRepository.findByIdWithMemberAndBoard(commentId)
        .orElseThrow(() -> new BaseException(ErrorCode.COMMENT_NOT_FOUND));

    // 2.권한 주체 판별
    Board board = comment.getBoard();
    boolean isAuthor = comment.getMember().getMemberId().equals(requesterId);
    boolean isBoardOwner = board.getMember().getMemberId().equals(requesterId);
    boolean isAdmin = isAdmin(requesterId);

    // 3.접근 권한 체크 : 세 주체 중 하나가 아니면 403
    if (!(isAuthor || isBoardOwner || isAdmin)) {
      throw new BaseException(ErrorCode.ACCESS_DENIED);
    }

    // 4.자식(대댓글) 존재 여부 확인
    boolean hasChild = boardCommentRepository.existsByParent(comment);

    // 5. 삭제 정책 분기
    if (hasChild) {
      // 자식(대댓글)이 있으면 :소프드 delete
      comment.markDeleted();
    } else {
      // 자식(대댓글)이 없으면 하드 delete 댓글 수 감소
      boardCommentRepository.delete(comment);
      board.decreaseCommentCount();
    }
  }

  // 댓글/대댓글 수정
  @Transactional
  public CommentResponseDto editComment(Long commentId, Long requesterId, String edit){

    // 1) 대상 댓글 로드(작성자/게시글까지 한 번에)
    BoardComment comment = boardCommentRepository.findByIdWithMemberAndBoard(commentId)
        .orElseThrow(() -> new BaseException(ErrorCode.COMMENT_NOT_FOUND));

    // 2) 요청자 로드(권한 체크용) — 비활성 사용자 차단은 JWT 필터에서 이미 처리
    Member requester = memberRepository.findById(requesterId)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

    // 3) 권한 체크: 댓글 작성자 || 게시글 작성자 || 관리자
    if (!isAuthor(comment, requester)) {
      throw new BaseException(ErrorCode.ACCESS_DENIED); // 403
    }

    // 4) 소프트삭제 댓글은 수정 금지
    if (isSoftDeleted(comment)) {
      throw new BaseException(ErrorCode.COMMENT_ALREADY_DELETED); // 409 등
    }

    // 5) 입력 검증/정규화(공백/길이/XSS)
    String normalized = validate(edit); // null/blank/max length/htmlEscape

    // 6) 동일 내용이면 DB UPDATE 생략 → 현 상태 그대로 응답
    if (normalized.equals(comment.getContent())) {
      return boardCommentMapper.toResponseDto(comment);
    }

    // 7) 더티체킹으로 수정(엔티티 도메인 메서드 사용)
    comment.editContent(normalized); // 내용만 변경 → modifiedDate는 Auditing이 자동세팅

    // 8) 수정 결과 반환
    return boardCommentMapper.toResponseDto(comment);
  }




}
