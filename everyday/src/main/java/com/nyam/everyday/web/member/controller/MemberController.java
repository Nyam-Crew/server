package com.nyam.everyday.web.member.controller;


import com.nyam.everyday.common.dto.CustomPageResponseDto;
import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.common.util.FileValidationUtils;
import com.nyam.everyday.module.badge.service.BadgeService;
import com.nyam.everyday.module.board.dto.BoardWithNicknameDto;
import com.nyam.everyday.module.board.service.BoardService;
import com.nyam.everyday.module.member.service.MemberService;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.web.badge.dto.AssignBadgeRequestDto;
import com.nyam.everyday.web.badge.dto.BadgeOwnershipDto;
import com.nyam.everyday.web.member.dto.*;
import com.nyam.everyday.web.member.mapper.MemberMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
@Slf4j
@Tag(name = "Member-Controller", description = "회원 관리")
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;
  private final BadgeService badgeService;
  private final BoardService boardService;
  private final MemberMapper memberMapper;

  @GetMapping("/{memberId}")
  @Operation(summary = "회원 정보", description = "회원의 정보를 조회합니다.")
  public ResponseEntity<MemberResponseDto> getMember(@PathVariable Long memberId) {
    MemberResponseDto memberResponseDto = memberService.getMemberById(memberId);
    return ResponseEntity.ok(memberResponseDto);
  }

  @GetMapping("/me")
  @Operation(summary = "로그인한 회원 정보", description = "로그인한 회원의 정보를 조회합니다.")
  public ResponseEntity<MemberResponseDto> getMember(@AuthenticationPrincipal CustomUserDetails userDetails) {
    Long id = userDetails.getId();
//    log.info("[getMember] memberId : {}", id);
    return ResponseEntity.ok(memberService.getMemberById(id));
  }

  @PostMapping
  @Operation(summary = "회원 추가", description = "회원을 신규 추가합니다.")
  public ResponseEntity<MemberResponseDto> create(@RequestBody MemberRequestDto dto) {
    MemberResponseDto saved = memberService.create(dto);
    return ResponseEntity.ok(saved);
  }

  @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "회원 정보 수정",
      description = "JSON 데이터와 이미지 파일을 함께 받아 회원 정보를 수정합니다.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          content = @Content(
              mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
              encoding = {
                  @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE),
                  @Encoding(name = "file", contentType = "image/png, image/jpeg")
              }
          )
      )
  )
  public ResponseEntity<MemberResponseDto> update (
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestPart("request") @jakarta.validation.Valid MemberRequestDto dto,
      @RequestPart(value = "file", required = false) MultipartFile file
  ) {
    if (userDetails == null) throw new BaseException(ErrorCode.AUTHENTICATION_FAILED);

    FileValidationUtils.validateOptionalPngJpeg(file, 5 * 1024 * 1024L); // 5MB 제한
    MemberResponseDto updated = memberService.update(userDetails.getId(), dto, file);
    return ResponseEntity.ok(updated);
  }

  @GetMapping("/check-nickname/{nickname}")
  @Operation(summary = "닉네임 중복 확인", description = "닉네임의 중복 여부를 확인합니다.")
  public ResponseEntity<NicknameDuplicationResponse> checkNicknameDuplication(@PathVariable String nickname) {
    NicknameDuplicationResponse response = memberService.checkNicknameDuplication(nickname);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{memberId}/badges")
  @Operation(summary = "회원에게 뱃지 부여", description = "특정 회원에게 뱃지를 부여합니다.")
  public ResponseEntity<Void> assignBadgeToMember(
      @PathVariable Long memberId,
      @RequestBody AssignBadgeRequestDto requestDto) {
    log.info("[assignBadgeToMember] memberId: {}, badgeId: {}", memberId, requestDto.getBadgeId());
    badgeService.assignBadgeToMember(memberId, requestDto);
    return ResponseEntity.ok().build();
  }


  /**
   * 페이징 입력 형식
   * {
   *   "page": 0,
   *   "size": 6,
   *   "sort":
   *     "createdDate"
   * }
   * */
  @GetMapping("/my-badges")
  @Operation(summary = "뱃지 목록 조회 (페이지네이션)", description = "페이지네이션된 뱃지 목록을 조회합니다. 현재 사용자의 소유 여부도 포함됩니다.")
  public ResponseEntity<CustomPageResponseDto<BadgeOwnershipDto>> getBadges(
      @PageableDefault(size = 6 ) Pageable pageable,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    log.info("[getBadges] Pageable request: {}", pageable);
    Sort sort = pageable.getSort().isUnsorted()
        ? Sort.by(Sort.Order.desc("createdDate"))
        : pageable.getSort();
    sort = sort.and(Sort.by("id").descending());
    Pageable fixed = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

    Long currentUserId = (userDetails != null) ? userDetails.getId() : null;
    log.info("[getBadges] 요청한 User ID: {}", currentUserId);

    Page<BadgeOwnershipDto> response = badgeService.getBadgeListWithOwnership(fixed, currentUserId);
    return ResponseEntity.ok(new CustomPageResponseDto<>(response));
  }

  @GetMapping("/me/badges/count")
  @Operation(summary = "획득한 뱃지 개수", description = "현재 사용자가 소유한 뱃지 총 개수")
  public ResponseEntity<BadgeCountResponse> getBadgesCnt(
      @AuthenticationPrincipal CustomUserDetails user) {
    if (user == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    long count = badgeService.getBadgesCnt(user.getId());
    return ResponseEntity.ok(new BadgeCountResponse(count));
  }

  @GetMapping("/my-boards")
  @Operation(summary = "내 게시글 목록 페이징", description = "로그인한 사용자의 게시글 목록을 페이징하여 조회합니다.")
  public ResponseEntity<CustomPageResponseDto<MyBoardsResponseDto>> getMyBoards(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
    if (userDetails == null) {
      throw new BaseException(ErrorCode.AUTHENTICATION_FAILED);
    }
    Page<BoardWithNicknameDto> myBoards =
        boardService.getMyBoards(userDetails.getId(), pageable);

    return ResponseEntity.ok(new CustomPageResponseDto<>(myBoards).map(memberMapper::toMyBoardsResponseDto));
  }

  @DeleteMapping("/me")
  @Operation(summary = "로그인한 회원 탈퇴", description = "로그인한 회원 탈퇴. 성공하면 204 No Content를 반환합니다.")
  public ResponseEntity<Void> deleteMember(@AuthenticationPrincipal CustomUserDetails userDetails) {
    Long id = userDetails.getId();
    log.info("[deleteMember] memberId : {}", id);
    memberService.deleteMember(id);
    return ResponseEntity.noContent().build();
  }

    @GetMapping("/me/id") // <-- 새로운 엔드포인트
    @Operation(summary = "로그인한 회원 ID 조회", description = "로그인한 회원의 memberId만 간단히 조회합니다.")
    public ResponseEntity<MemberIdResponseDto> getMemberId(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            // userDetails가 null일 경우 401 Unauthorized 반환
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new MemberIdResponseDto(userDetails.getId()));
    }

}

