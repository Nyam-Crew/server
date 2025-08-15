package com.nyam.everyday.module.member.service;


import com.nyam.everyday.common.aws.s3.entity.S3DefaultValue;
import com.nyam.everyday.common.aws.s3.service.AwsS3Service;
import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.common.util.HealthCalculator;
import com.nyam.everyday.common.util.HealthCalculator.HealthInfo;
import com.nyam.everyday.module.awsS3.dto.AwsS3Response;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.web.member.dto.MemberRequestDto;
import com.nyam.everyday.web.member.dto.MemberResponseDto;
import com.nyam.everyday.web.member.dto.NicknameDuplicationResponse;
import com.nyam.everyday.web.member.mapper.MemberMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

  private final AwsS3Service awsS3Service;
  private final MemberRepository memberRepository;
  private final MemberMapper memberMapper;


  public MemberResponseDto getMemberById(Long id) {
    Member member = memberRepository.findById(id)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "id " + id + "에 해당하는 사용자가 없습니다."));

    return getMemberResponseDto(member);
  }

  private MemberResponseDto getMemberResponseDto(Member member) {
    // HealthInfo 계산
    HealthInfo healthInfo = getHealthInfo(member);
    MemberResponseDto response = memberMapper.toDto(member);
    // DTO에 HealthInfo 설정 추가
    if(healthInfo!=null) {
      response.setBmi(healthInfo.bmi());
      response.setBmr(healthInfo.bmr());
      response.setTdee(healthInfo.tdee());
    }
    return response;
  }

  private HealthInfo getHealthInfo(Member member) {
    HealthInfo healthInfo = null;
    if(member.getHeight() !=null && member.getWeight() !=null && member.getAge()>5 ) {
      healthInfo = HealthCalculator.calculate(member);
      log.info("healthInfo : {}", healthInfo);
    } else {
      log.info("member height or weight or age is null");
    }
    return healthInfo;
  }

  @Transactional
  public MemberResponseDto create(MemberRequestDto dto) {
    Member entity = memberMapper.toEntity(dto);
    entity.setMemberImg(S3DefaultValue.DEFAULT_PROFILE_IMAGE.getValue());
    return memberMapper.toDto(memberRepository.save(entity));
  }


  @Transactional
  public MemberResponseDto update(Long id, MemberRequestDto dto) {
    Member member = memberRepository.findById(id)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "id " + id + "에 해당하는 사용자가 없습니다."));

    if(dto.getMemberImgFile() != null){
      AwsS3Response newS3Url = awsS3Service.replaceFile(member.getMemberImg(), dto.getMemberImgFile());
      dto.setMemberImg(newS3Url.getUrl());
    } else {
      dto.setMemberImg(S3DefaultValue.DEFAULT_PROFILE_IMAGE.getValue());
    }

    memberMapper.modify(dto, member);
    return getMemberResponseDto(member);
  }

  public NicknameDuplicationResponse checkNicknameDuplication(String nickname) {
    boolean isDuplicate = memberRepository.existsByNickname(nickname);
    return new NicknameDuplicationResponse(isDuplicate);
  }

}
