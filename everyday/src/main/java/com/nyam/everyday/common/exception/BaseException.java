package com.nyam.everyday.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
@Getter
@RequiredArgsConstructor
public class BaseException extends RuntimeException {

  public static final BaseException INVALID_INPUT_VALUE = new BaseException(ErrorCode.INVALID_INPUT_VALUE);
  public static final BaseException INVALID_TOKEN = new BaseException(ErrorCode.INVALID_TOKEN);
  public static final BaseException ALREADY_IN_BOOKMARK = new BaseException(ErrorCode.ALREADY_IN_BOOKMARK);
  public static final BaseException VALIDATION_FAILED = new BaseException(ErrorCode.ALREADY_EXIST_JOIN);
  public static final BaseException AUTHENTICATION_FAILED = new BaseException(ErrorCode.AUTHENTICATION_FAILED);
  public static final BaseException EXPIRED_ACCESS_TOKEN = new BaseException(ErrorCode.EXPIRED_ACCESS_TOKEN);
  public static final BaseException UNAUTHORIZED_ACCESS =  new BaseException(ErrorCode.UNAUTHORIZED_ACCESS);
  public static final BaseException MEMBER_ALREADY_DEACTIVATED = new BaseException(ErrorCode.MEMBER_ALREADY_DEACTIVATED);
  public static final BaseException CHAT_ROOM_ALREADY_LINKED = new BaseException(ErrorCode.CHAT_ROOM_ALREADY_LINKED);
  public static final BaseException ACCESS_DENIED = new BaseException(ErrorCode.ACCESS_DENIED);
  public static final BaseException MEMBER_DEACTIVATED = new BaseException(ErrorCode.MEMBER_DEACTIVATED);
  public static final BaseException INVALID_REFRESH_TOKEN = new BaseException(ErrorCode.INVALID_REFRESH_TOKEN);
  public static final BaseException REFRESH_TOKEN_MISMATCH = new BaseException(ErrorCode.REFRESH_TOKEN_MISMATCH);
  public static final BaseException MEMBER_NOT_FOUND =  new BaseException(ErrorCode.MEMBER_NOT_FOUND);
  public static final BaseException GROUP_NOT_FOUND = new BaseException(ErrorCode.GROUP_NOT_FOUND);
  public static final BaseException POST_NOT_FOUND = new BaseException(ErrorCode.POST_NOT_FOUND);
  public static final BaseException COMMENT_NOT_FOUND = new BaseException(ErrorCode.COMMENT_NOT_FOUND);
  public static final BaseException INTERNAL_SERVER_ERROR = new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
  public static final BaseException AWSS3_UPLOAD_ERROR =  new  BaseException(ErrorCode.AWSS3_UPLOAD_ERROR);
  public static final BaseException AWSS3_DELETE_ERROR = new  BaseException(ErrorCode.AWSS3_DELETE_ERROR);
  public static final BaseException BOARD_NOT_FOUND =  new BaseException(ErrorCode.BOARD_NOT_FOUND);

  private final ErrorCode errorCode;

  public BaseException(ErrorCode errorCode, String customMessage) {
    super(customMessage);
    this.errorCode = errorCode;
    log.error("[BaseException] {} ==> {}", errorCode.getCode(), customMessage);
  }

  // 의도적인 예외이므로 stack trace 제거 (불필요한 예외처리 비용 제거)
  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }


  public HttpStatus getHttpStatus() {return errorCode.getHttpStatus();}
}
