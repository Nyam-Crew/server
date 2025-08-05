package com.nyam.everyday.module.awsS3.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
public class AwsS3Request {

  // 새로 업로드할 이미지
  private MultipartFile newImage;
  // 기존 URL(삭제하고 새로 업로드하기 위함)
  private String oldUrl;
}
