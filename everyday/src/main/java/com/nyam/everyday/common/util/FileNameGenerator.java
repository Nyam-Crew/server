package com.nyam.everyday.common.util;

import java.util.UUID;
import org.springframework.stereotype.Component;



@Component
public class FileNameGenerator {

    /* AWS S3 저장시에 파일명으로 인해 파일이 덮어씌워지지 않도록 하기 위해 랜덤한 이름 생성함 */
    public String generateFileName(String originalFilename) {
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID() + ext;
    }

    /** 범용: 확장자 없이 UUID 문자열만 반환 (피드 ID 등에 사용) */
    public String generateUuid() {
        return UUID.randomUUID().toString();
    }
}
