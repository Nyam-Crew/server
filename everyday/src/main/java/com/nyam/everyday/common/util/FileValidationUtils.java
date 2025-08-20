package com.nyam.everyday.common.util;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 이미지 업로드 검증 유틸리티.
 * - PNG/JPEG만 허용
 * - 크기 제한
 * - 확장자/Content-Type 화이트리스트
 * - 매직 넘버(파일 시그니처) 검증
 */
public final class FileValidationUtils {

  private FileValidationUtils() {}

  public static final long DEFAULT_MAX_BYTES = 5 * 1024 * 1024L; // 5MB 예시
  public static final Set<String> ALLOWED_CT_PNG_JPEG = Set.of("image/png", "image/jpeg", "image/jpg");
  public static final Set<String> ALLOWED_EXT_PNG_JPEG = Set.of("png", "jpg", "jpeg");

  /** file이 null/empty면 검증 생략, 있으면 PNG/JPEG 검증 수행 */
  public static void validateOptionalPngJpeg(MultipartFile file) {
    validateOptionalPngJpeg(file, DEFAULT_MAX_BYTES);
  }

  /** file이 null/empty면 검증 생략, 있으면 PNG/JPEG 검증 수행(최대 용량 지정) */
  public static void validateOptionalPngJpeg(MultipartFile file, long maxBytes) {
    if (file == null || file.isEmpty()) return;
    validatePngJpeg(file, maxBytes);
  }

  /** PNG/JPEG만 허용 (필수 파일) */
  public static void validatePngJpeg(MultipartFile file, long maxBytes) {
    if (file == null || file.isEmpty()) {
      throw new BaseException(ErrorCode.INVALID_FILE_TYPE);
    }

    // 1) 크기 제한
    if (file.getSize() > maxBytes) {
      throw new BaseException(ErrorCode.FILE_TOO_LARGE);
    }

    // 2) 확장자 화이트리스트
    String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
    if (ext == null || !ALLOWED_EXT_PNG_JPEG.contains(ext.toLowerCase())) {
      throw new BaseException(ErrorCode.INVALID_FILE_TYPE);
    }

    // 3) Content-Type 화이트리스트
    String ct = Optional.ofNullable(file.getContentType()).orElse("").toLowerCase();
    if (!ALLOWED_CT_PNG_JPEG.contains(ct)) {
      throw new BaseException(ErrorCode.INVALID_FILE_TYPE);
    }

    // 4) 매직 넘버(시그니처) 검사
    if (!hasPngOrJpegSignature(file)) {
      throw new BaseException(ErrorCode.INVALID_FILE_TYPE);
    }
  }

  /** 파일 헤더의 매직 넘버로 PNG/JPEG 여부를 확인 */
  public static boolean hasPngOrJpegSignature(MultipartFile file) {
    try (InputStream in = file.getInputStream()) {
      byte[] header = in.readNBytes(16);
      return isPngHeader(header) || isJpegHeader(header);
    } catch (IOException e) {
      // 필요 시 ErrorCode를 FILE_IO_ERROR 등으로 변경
      throw new BaseException(ErrorCode.INVALID_FILE_TYPE);
    }
  }

  private static boolean isPngHeader(byte[] h) {
    // PNG: 89 50 4E 47 0D 0A 1A 0A
    return h != null && h.length >= 8
        && (h[0] & 0xFF) == 0x89 && h[1] == 0x50 && h[2] == 0x4E && h[3] == 0x47
        && h[4] == 0x0D && h[5] == 0x0A && h[6] == 0x1A && h[7] == 0x0A;
  }

  private static boolean isJpegHeader(byte[] h) {
    // JPEG: FF D8 (SOI)
    return h != null && h.length >= 2
        && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8;
  }
}