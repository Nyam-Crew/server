package com.nyam.everyday.module.awsS3.controller;

import com.nyam.everyday.module.awsS3.dto.AwsS3Response;
import com.nyam.everyday.common.aws.s3.service.AwsS3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/s3")
@Tag(name = "Admin-S3-Controller", description = "기본으로 서버에 존재해야 할 파일 업로드를 위해 생성한 컨트롤러입니다.")
@RequiredArgsConstructor
class AdminS3Controller {

  private final AwsS3Service awsS3Service;

  @Operation(summary = "어드민 전용 파일 업로드", description = "기본 파일 파일 업로드를 위해 사용하는 endpoint입니다. 일반 파일 업로드는 AwsS3Service.replaceFile을 사용해주세요")
  @PostMapping("/upload")
  public AwsS3Response uploadFile(MultipartFile newImage) {
    return awsS3Service.uploadFile(newImage);
  }
}
