package com.nyam.everyday.module.member.service;


import com.nyam.everyday.common.aws.s3.entity.S3DefaultValue;
import com.nyam.everyday.common.aws.s3.service.AwsS3Service;
import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.common.util.HealthCalculator;
import com.nyam.everyday.common.util.HealthCalculator.HealthInfo;
import com.nyam.everyday.module.awsS3.dto.AwsS3Response;
import com.nyam.everyday.module.challenge.entity.ChallengeTag;
import com.nyam.everyday.module.challenge.checker.event.event.ChallengeCheckEvent;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.entity.Status;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.web.member.dto.MemberRequestDto;
import com.nyam.everyday.web.member.dto.MemberResponseDto;
import com.nyam.everyday.web.member.dto.NicknameDuplicationResponse;
import com.nyam.everyday.web.member.mapper.MemberMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
  private final ApplicationEventPublisher publisher;

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

    // 목표 체중 기반 권장 섭취칼로리 계산
    if(member.getTargetWeight().compareTo(BigDecimal.ZERO) > 0 && healthInfo != null && healthInfo.tdee().compareTo(BigDecimal.ZERO) > 0){
      BigDecimal currentWeight = member.getWeight();
      BigDecimal targetWeight = member.getTargetWeight();
      BigDecimal tdee = healthInfo.tdee();
      
      BigDecimal recommendedCaloriesBD;
      
      // 목표 체중에 따른 칼로리 조정
      if (targetWeight.compareTo(currentWeight) < 0) {
        // 체중 감량 목표: TDEE - 500 kcal
        recommendedCaloriesBD = tdee.subtract(new BigDecimal("500"));
        log.info("체중 감량 목표 - 현재: {}kg, 목표: {}kg, 권장 칼로리: {}", currentWeight, targetWeight, recommendedCaloriesBD);
      } else if (targetWeight.compareTo(currentWeight) > 0) {
        // 체중 증량 목표: TDEE + 500 kcal
        recommendedCaloriesBD = tdee.add(new BigDecimal("500"));
        log.info("체중 증량 목표 - 현재: {}kg, 목표: {}kg, 권장 칼로리: {}", currentWeight, targetWeight, recommendedCaloriesBD);
      } else {
        // 체중 유지 목표: TDEE 그대로
        recommendedCaloriesBD = tdee;
        log.info("체중 유지 목표 - 현재: {}kg, 목표: {}kg, 권장 칼로리: {}", currentWeight, targetWeight, recommendedCaloriesBD);
      }
      
      // 가장 가까운 정수로 반올림하여 Integer로 변환
      Integer recommendedCalories = recommendedCaloriesBD.setScale(0, RoundingMode.HALF_UP).intValue();
      response.setRecommendedCalories(recommendedCalories);
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
    log.info("memberId : {}, dto : {}, file : {}", memberId, dto, file);
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "id " +memberId+ "에 해당하는 사용자가 없습니다."));

    if(file != null){
      // 사진 파일 있는 경우 교체
      AwsS3Response newS3Url;
      if(dto.getMemberImg() != null){
        // 기존 이미지 있는 경우 교체
        newS3Url = awsS3Service.replaceFile(member.getMemberImg(), file);
        log.info("프로필 사진 교체 : {}" , newS3Url.getUrl());
      } else {
        // 기존 이미지 없는경우 신규 업로드
        newS3Url = awsS3Service.uploadFile(file);
        log.info("프로필 사진 신규 업로드 : {}" , newS3Url.getUrl());
      }
      dto.setMemberImg(newS3Url.getUrl());
    } else if(dto.getMemberImg() != null && !dto.getMemberImg().isBlank() && dto.getMemberImg().contains(".")){
      // 파일 없으니 기존 이미지 있는 경우 유지
      dto.setMemberImg(dto.getMemberImg());
      log.info("프로뢸 사진 유지 : {}" , dto.getMemberImg());
    } else {
      // 파일 없고 기존 이미지도 없으면 기본 이미지.
      dto.setMemberImg(S3DefaultValue.DEFAULT_PROFILE_IMAGE.getValue());
      log.info("프로필 사진 없음 - 기본 프로필 설정");
    }
    memberMapper.modify(dto, member);

    publisher.publishEvent(new ChallengeCheckEvent(memberId, ChallengeTag.PROFILE, LocalDate.now()));

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

    if (lastLogin == null || member.getConsecutiveLoginDays() == 0) {
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

    // 챌린지 달성 여부 확인을 위한 이벤트 발행
    publisher.publishEvent(new ChallengeCheckEvent(memberId, ChallengeTag.LOGIN, LocalDate.now()));
    log.info("로그인 기반 챌린지 체크 이벤트 발행 성공");
  }

  @Transactional
  public void deleteMember(Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "id " + memberId + "에 해당하는 사용자가 없습니다."));
    member.setMemberStatus(Status.DEACTIVATED);
  }

  public Member getMemberByMemberId(Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "memberId " + memberId + "에 해당하는 사용자가 없습니다."));
    return member;
  }

}

