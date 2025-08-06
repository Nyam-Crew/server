package com.nyam.everyday.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  /* 400 - Bad Request */
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "입력 값이 올바르지 않습니다."),
  INVALID_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
  ALREADY_IN_BOOKMARK(HttpStatus.BAD_REQUEST, "ALREADY_IN_BOOKMARK", "이미 북마크에 존재하는 게시글입니다"),
  ALREADY_EXIST_JOIN(HttpStatus.BAD_REQUEST, "ALREADY_EXIST_JOIN", "이미 신청한 그룹입니다."),
  VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "입력값 유효성 검사에 실패했습니다."),

  /* 401 - Unauthorized */
  AUTHENTICATION_FAILED(HttpStatus.BAD_REQUEST, "AUTHENTICATION_FAILED", "인증에 실패했습니다."),
  EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_ACCESS_TOKEN", "Access Token이 만료되었습니다. 토큰을 재발급해주세요"),
  UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED_ACCESS","권한이 없습니다"),
  MEMBER_ALREADY_DEACTIVATED(HttpStatus.BAD_REQUEST,"MEMBER_ALREADY_DEACTIVATED","이미 비활성화 된 회원입니다"),
  CHAT_ROOM_ALREADY_LINKED(HttpStatus.BAD_REQUEST,"CHAT_ROOM_ALREADY_LINKED","이미 연결 된 채팅방 입니다"),

  /* 403 - Forbidden */
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "접근 권한이 없습니다."),
  MEMBER_DEACTIVATED(HttpStatus.UNAUTHORIZED,"MEMBER_DEACTIVATED","비활성화된 멤버입니다"),
  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"INVALID_REFRESH_TOKEN","유효하지 않는 토큰입니다"),
  REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED,"REFRESH_TOKEN_MISMATCH","리프레쉬 토큰이 불일치 합니다"),

  /* 404 - Not Found */
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,"MEMBER_NOT_FOUND","해당 사용자를 찾을 수 없습니다"),
  GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "GROUP_NOT_FOUND", "해당 모임을 찾을 수 없습니다."),
  POST_NOT_FOUND(HttpStatus.BAD_REQUEST, "POST_NOT_FOUND", "게시글을 찾을 수 없습니다."),
  COMMENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다."),

  /* 500 - Internal Server Error */
  AWSS3_UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AWSS3_UNKNOWN_ERROR", "S3 연결 과정에서 에러가 발생했습니다"),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버에 오류가 발생했습니다."),
  AWSS3_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AWSS3_UPLOAD_ERROR", "S3에 업로드하는 과정에서 에러가 발생했습니다"),
  AWSS3_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AWSS3_DELETE_ERROR", "S3에서 파일을 삭제하는 과정에서 에러가 발생했습니다");




  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
