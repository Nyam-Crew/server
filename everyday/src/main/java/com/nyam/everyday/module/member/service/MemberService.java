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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
  public MemberResponseDto update(Long memberId, MemberRequestDto dto, MultipartFile file) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "id " +memberId+ "에 해당하는 사용자가 없습니다."));

    if(file != null){
      AwsS3Response newS3Url = awsS3Service.replaceFile(member.getMemberImg(), file);
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



  /** 로그인 연속 출석 정보 업데이트 */
  @Transactional
  public void updateLoginInfo(Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "로그인 정보 업데이트 중 회원을 찾을 수 없습니다."));

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime lastLogin = member.getLastLoginDate();

    if (lastLogin == null) {
      // 1. 첫 로그인이거나, 기능 추가 후 첫 로그인인 경우
      member.setConsecutiveLoginDays(1);
      log.info("첫 로그인 memberId : {}", memberId);
    } else {
      // 2. 마지막 로그인 날짜와 오늘 날짜를 비교 (시간은 제외하고 날짜만 비교)
      LocalDate todayDate = now.toLocalDate();
      LocalDate lastLoginDate = lastLogin.toLocalDate();

      // 같은 날에 여러 번 로그인하는 경우는 횟수를 올리지 않음
      if (!lastLoginDate.isEqual(todayDate)) {
        if (lastLoginDate.isEqual(todayDate.minusDays(1))) {
          // 1. 마지막 로그인이 어제인 경우 (연속 로그인)
          member.setConsecutiveLoginDays(member.getConsecutiveLoginDays() + 1);
          log.info("연속 로그인 memberId : {}, 연속 날짜 : {}", memberId, member.getConsecutiveLoginDays());
          // TODO 연속 날짜 뱃지 대상인지 체크 로직 추가해야함 (ex. 7일 이상 연속출석)
        } else {
          // 2. 연속 로그인이 끊긴 경우
          member.setConsecutiveLoginDays(1);
        }
      }
    }
    // 마지막 로그인 시간을 현재 시간으로 업데이트
    member.setLastLoginDate(now);
  }
}
