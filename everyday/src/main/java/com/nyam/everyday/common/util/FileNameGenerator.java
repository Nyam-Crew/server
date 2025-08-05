package com.nyam.everyday.common.util;

import java.util.UUID;
import org.springframework.stereotype.Component;


/* AWS S3 저장시에 파일명으로 인해 파일이 덮어씌워지지 않도록 하기 위해 랜덤한 이름 생성함 */
@Component
public class FileNameGenerator {

  public String generateFileName(String originalFilename) {
    String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
    return UUID.randomUUID() + ext;
  }
}
