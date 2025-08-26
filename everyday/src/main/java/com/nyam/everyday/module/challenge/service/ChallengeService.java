package com.nyam.everyday.module.challenge.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.module.challenge.entity.Challenge;
import com.nyam.everyday.module.challenge.entity.ChallengeType;
import com.nyam.everyday.module.challenge.repository.ChallengeRepository;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.web.challenge.dto.EventAttendDto;
import com.nyam.everyday.web.challenge.dto.EventChallengeDto;
import com.nyam.everyday.web.challenge.dto.RegularChallengeDto;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChallengeService {

  private final MemberRepository memberRepository;
  private final ChallengeRepository challengeRepository;
  private final MemberChallengeStatusService mcsService;

  /**
   * 사용자가 현재 진행 중이며 아직 완료하지 못한 챌린지 목록을 조회합니다.
   *
   * <p>구현 예정: MemberChallengeStatus 기준으로 진행 중 판별 후 Challenge를 모아 DTO로 변환합니다.</p>
   *
   * @param memberId 조회 대상 회원의 식별자
   * @return 진행 중인 챌린지의 DTO 목록
   */
  // 상시 챌린지 리스트에 표시될 리스트를 전달한다.
  public List<RegularChallengeDto> getProgressingChallenge(Long memberId) {
    // 유저가 진행중이면서, 아직 완료하지 못한 챌린지를 n개 제공한다.
    List<Challenge> challenges = challengeRepository.findAll();
    List<RegularChallengeDto> dtos = this.makeChallengeToRegularDto(memberId, challenges);

    List<RegularChallengeDto> result = new ArrayList<>();
    // 진행도가 0이 아닌 챌린지 최대 2개까지
    for (RegularChallengeDto dto : dtos) {
      if (result.size() >= 2) {
        break;
      }
      if (dto.getProgressCount() == 0) {
        break;
      }

      result.add(dto);
    }

    return result;
  }

  /**
   * 상시(정규) 챌린지 전체 목록을 조회해 사용자의 진행 상황을 포함한 DTO로 반환합니다.
   *
   * <p>ChallengeType.REGULAR_CHALLENGE 에 해당하는 엔티티를 조회하고
   * 각 챌린지에 대해 완료 여부와 진행 개수를 조회한 뒤 DTO로 변환합니다.</p>
   *
   * @param memberId 진행 상황을 함께 계산할 회원의 식별자
   * @return 상시 챌린지 DTO 목록
   */
  public List<RegularChallengeDto> getRegularChallengeList(Long memberId) {
    // 먼저 모든 챌린지 리스트를 가져온다.
    List<Challenge> challenges = challengeRepository.getAllByType(ChallengeType.REGULAR_CHALLENGE);
    // Dto로 변환한다.
    List<RegularChallengeDto> result = this.makeChallengeToRegularDto(memberId, challenges);
    // 반환
    return result;
  }

  /**
   * 이벤트성 챌린지 전체 목록을 조회해 사용자의 진행 상황을 포함한 DTO로 반환합니다.
   *
   * <p>ChallengeType.EVENT_CHALLENGE 에 해당하는 엔티티를 조회하고
   * 각 챌린지에 대해 완료 여부와 진행 개수를 조회한 뒤 DTO로 변환합니다.</p>
   *
   * @param memberId 진행 상황을 함께 계산할 회원의 식별자
   * @return 이벤트 챌린지 DTO 목록
   */
  public List<EventChallengeDto> getEventChallengeList(Long memberId) {
    // 먼저 모든 챌린지 리스트를 가져온다.
    List<Challenge> challenges = challengeRepository.getAllByType(ChallengeType.EVENT_CHALLENGE);
    // Dto로 변환한다.
    List<EventChallengeDto> result = this.makeChallengeToEventDto(memberId, challenges);
    // 반환
    return result;
  }


  /**
   * Challenge 엔티티 리스트를 사용자의 진행 상황을 반영한 ChallengeDto 리스트로 변환합니다.
   *
   * @param memberId   진행 상황을 조회할 회원 식별자
   * @param challenges 변환 대상 챌린지 엔티티 목록
   * @return 변환된 ChallengeDto 목록
   */
  private List<RegularChallengeDto> makeChallengeToRegularDto(Long memberId,
      List<Challenge> challenges) {
    List<RegularChallengeDto> result = new ArrayList<>();

    // 각 챌린지별 Dto 만들기
    for (Challenge challenge : challenges) {
      // 유저의 챌린지 완료 여부를 불러온다.
      boolean isCleared = mcsService.getIsCleared(memberId, challenge.getId());
      Long progressCount = mcsService.getProgressCount(memberId, challenge.getId());

      // null-safe LocalDate 변환
      java.time.LocalDate startDate =
          challenge.getStartDate() != null ? challenge.getStartDate().toLocalDate() : null;
      java.time.LocalDate endDate =
          challenge.getEndDate() != null ? challenge.getEndDate().toLocalDate() : null;

      // Dto 생성해서, result에 집어넣는다
      result.add(
          RegularChallengeDto.builder().challengeId(challenge.getId()).title(challenge.getTitle())
              .description(challenge.getDescription()).cleared(isCleared)
              .targetCount(challenge.getTargetCount()).progressCount(progressCount).build());
    }

    // 진행도 기반 정렬한다
    // 1) 아직 안 깬 챌린지(false)가 먼저, 이미 깬 챌린지(true)는 아래로
    // 2) 같은 그룹 내에서는 진행률(= progressCount / targetCount) 내림차순
    result.sort(java.util.Comparator.comparingInt(
            (RegularChallengeDto d) -> d.isCleared() ? 1 : 0) // false(0) 먼저 true(1) 나중
        .thenComparing(java.util.Comparator.<RegularChallengeDto>comparingDouble(d -> {
          long p = d.getProgressCount() == null ? 0L : d.getProgressCount();
          long t = d.getTargetCount() == null ? 0L : d.getTargetCount();
          return t <= 0 ? 0.0 : (double) p / (double) t;
        }).reversed()));

    return result;
  }


  /**
   * Challenge 엔티티 리스트를 사용자의 진행 상황을 반영한 ChallengeDto 리스트로 변환합니다.
   *
   * @param memberId   진행 상황을 조회할 회원 식별자
   * @param challenges 변환 대상 챌린지 엔티티 목록
   * @return 변환된 ChallengeDto 목록
   */
  private List<EventChallengeDto> makeChallengeToEventDto(Long memberId,
      List<Challenge> challenges) {
    List<EventChallengeDto> result = new ArrayList<>();

    // 각 챌린지별 Dto 만들기
    for (Challenge challenge : challenges) {
      // 유저의 챌린지 완료 여부를 불러온다.
      boolean isCleared = mcsService.getIsCleared(memberId, challenge.getId());
      Long progressCount = mcsService.getProgressCount(memberId, challenge.getId());
      boolean isAttending = mcsService.isAttending(memberId, challenge.getId());

      // null-safe LocalDate 변환
      java.time.LocalDate startDate =
          challenge.getStartDate() != null ? challenge.getStartDate().toLocalDate() : null;
      java.time.LocalDate endDate =
          challenge.getEndDate() != null ? challenge.getEndDate().toLocalDate() : null;

      // Dto 생성해서, result에 집어넣는다
      result.add(
          EventChallengeDto.builder().challengeId(challenge.getId()).title(challenge.getTitle())
              .description(challenge.getDescription()).cleared(isCleared)
              .targetCount(challenge.getTargetCount()).progressCount(progressCount)
              .startDate(startDate).endDate(endDate).attending(isAttending).build());
    }

    // 이벤트 챌린지를 정렬한다.
    // 1) 현재 참여 가능한 챌린지가 위로 (ongoing)
    // 2) 현재 참여 중인 챌린지가 위로
    // 3) 클리어 못한 챌린지가 더 위로
    // 3) nowDate < startDate (진행 예정) 이 위로
    // 4) nowDate > endDate (종료) 는 아래로 밀기
    final java.time.LocalDate now = java.time.LocalDate.now();
    result.sort(java.util.Comparator
        // 1) 현재 참여 가능한 챌린지가 위로 (ongoing)
        .comparingInt((EventChallengeDto d) -> {
          java.time.LocalDate s = d.getStartDate();
          java.time.LocalDate e = d.getEndDate();
          boolean notStarted = (s != null) && now.isBefore(s);
          boolean ended = (e != null) && now.isAfter(e);
          boolean ongoing = !notStarted && !ended;
          return ongoing ? 0 : 1; // 0이 먼저
        })
        // 2) 현재 참여 중인 챌린지가 위로
        .thenComparingInt(d -> d.isAttending() ? 0 : 1)
        // 3) 클리어 못한 챌린지가 더 위로
        .thenComparingInt(d -> d.isCleared() ? 1 : 0)
        // 3) nowDate < startDate (진행 예정) 이 위로
        .thenComparingInt(d -> {
          java.time.LocalDate s = d.getStartDate();
          boolean notStarted = (s != null) && now.isBefore(s);
          return notStarted ? 0 : 1;
        })
        // 4) nowDate > endDate (종료) 는 아래로 밀기
        .thenComparingInt(d -> {
          java.time.LocalDate e = d.getEndDate();
          boolean ended = (e != null) && now.isAfter(e);
          return ended ? 1 : 0; // ended == true 이면 뒤로
        })
        // 진행률 높은 순 (동률 정렬용)
        .thenComparing(java.util.Comparator.<EventChallengeDto>comparingDouble(d -> {
          long p = d.getProgressCount() == null ? 0L : d.getProgressCount();
          long t = d.getTargetCount() == null ? 0L : d.getTargetCount();
          return t <= 0 ? 0.0 : (double) p / (double) t;
        }).reversed()));

    return result;
  }

  // 이벤트 챌린지에 참여 처리합니다.
  public void attendToEventChallenge(Long memberId, EventAttendDto dto) {
    Member member = memberRepository.findByMemberId(memberId)
        .orElseThrow(() -> BaseException.MEMBER_NOT_FOUND);
    Challenge challenge = challengeRepository.findById(dto.getChallengeId())
        .orElseThrow(() -> BaseException.CHALLENGE_NOT_FOUND);

    mcsService.attendToEventChallenge(member, challenge);
  }
}
