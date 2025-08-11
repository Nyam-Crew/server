package com.nyam.everyday.common.aws.s3.service;

import com.nyam.everyday.module.awsS3.dto.AwsS3Response;
import com.nyam.everyday.common.aws.s3.entity.S3DefaultValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AwsS3Service {

  // AWS 파일 업로더 불러오기
  private final AwsS3Uploader awsS3Uploader;

  // S3에 그냥 파일을 업로드한다.
  public AwsS3Response uploadFile(MultipartFile file) {
    return AwsS3Response.builder().url(awsS3Uploader.upload(file)).build();
  }

  // S3에 파일을 업로드하고, 기존 파일은 삭제한다
  // 매개변수로는 새로 업로드할 파일, 기존의 파일 경로가 들어오게 됨
  public AwsS3Response replaceFile(String existingUrl, MultipartFile fileToUpload) {
      // 1) 새 파일 업로드 먼저
      String newUrl = awsS3Uploader.upload(fileToUpload);

      // 2) 기존 파일이 기본 이미지가 아니고, null 아니고, 새 URL과 다르면 삭제
      if (existingUrl != null
              && !S3DefaultValue.contains(existingUrl)
              && !existingUrl.equals(newUrl)) {
          awsS3Uploader.delete(existingUrl);
      }

      // 3) 새 URL 반환
      return AwsS3Response.builder().url(newUrl).build();
  }
}
