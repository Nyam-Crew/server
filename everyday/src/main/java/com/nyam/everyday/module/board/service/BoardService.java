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
import com.nyam.everyday.search.board.service.BoardSearchIndexer;
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
import lombok.RequiredArgsConstructor;
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
  private final BoardSearchIndexer boardSearchIndexer;
  private final BoardSearchService boardSearchService;

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
    boardSearchIndexer.index(board);//ES 색인 동기화

    return boardMapper.toDto(saved);


  }
  //게시글 삭제
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
    boardSearchIndexer.delete(boardId);
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
  public BoardDetailDto getBoard(Long boardId) {
    // 1) 작성자 정보까지 함께 조회
    Board board = boardRepository.findWithMemberByBoardId(boardId)
        .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

    // 2) 조회수 증가 (더티체킹으로 커밋 시 반영)
    board.increaseViewCount();

    // (선택) 로그인 사용자의 좋아요 여부를 구해 내려주고 싶다면 여기서 계산:
    // Boolean likedByMe = boardLikeService.hasLiked(currentMemberId, boardId);
    Boolean likedByMe = null;

    // 3) 상세 DTO 변환
    return BoardDetailDto.from(board, likedByMe);
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
    //boolean changed = false;

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

    boardSearchIndexer.index(board);
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
    // 1.유효성 검사
    if (q == null || q.isBlank()){            // 검색어 유효성
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
    }
    //String keyword = q.trim().toLowerCase(); // 공백 제거 + 소문자 통일 -> 쿼리 검색로직
    String keyword = q.trim(); // ES analyzer가 처리하므로 소문자 변환 불필요
    String normType = (boardType == null || boardType.isBlank()) ? null : boardType.trim();
    String escaped = escapeLike(keyword);   // Like 와일드카드 인젝션 방지

    // 2.ES 검색 스코프 정규화
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

    /** JPQL검색 로직
     // 2.검색 스코프 정규화
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

     */
    // 3. 타입 화이트리스트 검증
    if (boardType != null && !isValidBoardType(boardType)) {
      throw new BaseException(ErrorCode.INVALID_BOARD_TYPE);
    }

    // 4.ES 검색 실행
    var esPage = boardSearchService.search(keyword,normType,f,pageable);

    // 5.ES 결과 id 추출
    List<Long> idsInOrder = esPage.getContent().stream()
        .map(BoardDocument::getBoardId)
        .filter(Objects::nonNull)
        .toList();
    if (idsInOrder.isEmpty()){
      return Page.empty(pageable);
    }
    // 6) DB 재조회 + ES 순서 유지 정렬
    List<Board> boards = boardRepository.findAllById(idsInOrder);
    Map<Long, Integer> order = new HashMap<>();
    for (int i = 0; i < idsInOrder.size(); i++) order.put(idsInOrder.get(i), i);
    boards.sort(Comparator.comparingInt(b -> order.getOrDefault(b.getBoardId(), Integer.MAX_VALUE)));

    // 7) 엔티티 → DTO 매핑
    List<BoardPageDto> content = boards.stream()
        .map(boardMapper::toPageDto)
        .toList();



    return new PageImpl<>(content,pageable,esPage.getTotalElements());
  }

}


