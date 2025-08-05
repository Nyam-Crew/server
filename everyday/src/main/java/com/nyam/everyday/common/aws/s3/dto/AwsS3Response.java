package com.nyam.everyday.common.aws.s3.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AwsS3Response {

  // 저장된 결과 URL
  private String url;
}
