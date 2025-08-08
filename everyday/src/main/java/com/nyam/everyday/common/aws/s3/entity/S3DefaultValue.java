package com.nyam.everyday.common.aws.s3.entity;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum S3DefaultValue {

  DEFAULT_PROFILE_IMAGE("https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/1bf63ab8-fb3c-4baa-897c-3d1a91edccca.png"),
  DEFAULT_GROUP_IMAGE("https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/1f08ffc8-8943-459e-97dc-94dccde5a7d3.jpg"),
  DEFAULT_BADGE_IMAGE("https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png"),
  DEFAULT_STAMP_IMAGE("https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/e36d1120-ab6c-4b1f-a405-f2c53c21da7a.png");

    private final String value;

    /** targetUrl이 기본 이미지 중 하나면 true */
    public static boolean contains(String targetUrl) {
        if (targetUrl == null) return false;
        return Arrays.stream(values())
                .anyMatch(defaultImage -> defaultImage.value.equals(targetUrl));
    }
}
