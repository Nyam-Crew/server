package com.nyam.everyday.module.board.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.board.dto.BoardWithNicknameDto;
import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.module.board.repository.BoardRepository;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.entity.Status;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.security.core.Role;
import com.nyam.everyday.web.board.dto.BoardPageDto;
import com.nyam.everyday.web.board.dto.BoardResponseDto;
import com.nyam.everyday.web.board.dto.CreateBoardRequestDto;
import com.nyam.everyday.web.board.dto.EditBoardRequestDto;
import com.nyam.everyday.web.board.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    // 2.소유자 검증: 현재 로그인한 회원이 작성자가 아니면 예외 발생
    if (!board.getMember().getMemberId().equals(memberId)) {
      throw new BaseException(ErrorCode.ACCESS_DENIED);
    }

    //3 삭제
    boardRepository.delete(board);

  }


  @Transactional(readOnly = true)
  public Page<BoardPageDto> getBoardPreviews(String boardType, Pageable pageable) {

    if (boardType != null && !isValidBoardType(boardType)) {
      throw new BaseException(ErrorCode.INVALID_BOARD_TYPE);
    }
    return boardRepository.findBoardPreviews(boardType,pageable);
  }

  // 게시글 조회
  @Transactional
  public BoardResponseDto getBoard(Long boardId) {
    Board board = boardRepository.findWithMemberByBoardId(boardId)
        .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

    board.increaseViewCount(); // Board 엔티티 내부에서 +1 증가
    return boardMapper.toDto(board);

  }

  @Transactional(readOnly = true)
  public Page<BoardWithNicknameDto> getMyBoards(Long memberId, Pageable pageable) {
    return boardRepository.findByMemberId(memberId, pageable);
  }

  //게시글 수정
  @Transactional
  public BoardResponseDto editBoard(Long boardId, Long requesterId, EditBoardRequestDto dto){
    // 1.요청자 조회 (관리자 판별용) - 비활성 유저 차단은 JWT 필터에서 이미 처리되지만, 권한 확인은 서비스에서
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
    // 4.변경 감지 플래그(동일값이면 업데이트 스킵용) - 선택

    if (dto.getTitle() != null){
      String title = normalizeNonBlank(dto.getTitle());
      String safeTitle = HtmlUtils.htmlEscape(title);
      if (!safeTitle.equals(board.getBoardTitle())){
        board.setBoardTitle(safeTitle);
      }
    }
    if (dto.getContent() != null){
      String content = normalizeNonBlank(dto.getContent());
      String safe = HtmlUtils.htmlEscape(content);
      if (!safe.equals(board.getBoardContent())){
        board.setBoardContent(safe);
      }
    }

    return boardMapper.toDto(board);
  }


  // 게시글 검색(제목/내용/닉네임/전체 등으로 검색)
  @Transactional(readOnly = true)
  public  Page<BoardPageDto> searchBoards(
            String q, //검색어
            String boardType, //게시판 타입
            Pageable pageable,//페이징/정렬
            String field //검색 대상
  ) {
    if (q == null || q.isBlank()){            // 1.검색어 유효성
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
    }
    String keyword = q.trim().toLowerCase(); // 2.공백 제거 + 소문자 통일
    String escaped = escapeLike(keyword);   // 3.Like 와일드카드 인젝션 방지


    // 4.검색 대상 플레그 계산
    boolean inTitle;
    boolean inContent;
    boolean inNick;
    String f = (field == null || field.isBlank()) ? "titlecontent" : field.trim().toLowerCase();

    switch (f) {
      case "title"        -> { inTitle = true;  inContent = false; inNick = false; }
      case "content"      -> { inTitle = false; inContent = true;  inNick = false; }
      case "nickname"     -> { inTitle = false; inContent = false; inNick = true;  }
      case "titlecontent" -> { inTitle = true;  inContent = true;  inNick = false; } // 기본
      default             -> throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
    }
    // 5. 타입 화이트리스트 검증
    if (boardType != null && !isValidBoardType(boardType)) {
      throw new BaseException(ErrorCode.INVALID_BOARD_TYPE);
    }

    // 6.레포 호출 (레포 JPQL에서 concat('%', :q, '%') escape '!' 사용 중)
    Page<Board> page = boardRepository.searchByField(
        escaped,
        boardType,
        inTitle,inContent,inNick,
        pageable
    );
    return page.map(boardMapper::toPageDto);

  }

}


