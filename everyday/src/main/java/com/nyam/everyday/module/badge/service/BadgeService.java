package com.nyam.everyday.module.badge.service;

import static com.nyam.everyday.common.aws.s3.entity.S3DefaultValue.DEFAULT_BADGE_IMAGE;

import com.nyam.everyday.common.aws.s3.service.AwsS3Service;
import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.awsS3.dto.AwsS3Response;
import com.nyam.everyday.module.badge.entity.Badge;
import com.nyam.everyday.module.badge.entity.MemberBadgeStatus;
import com.nyam.everyday.module.badge.repository.BadgeRepository;
import com.nyam.everyday.module.badge.repository.MemberBadgeStatusRepository;
import com.nyam.everyday.module.badge.dto.OwnedBadgeDto;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.scorelog.entity.SourceType;
import com.nyam.everyday.module.scorelog.service.ScoreLogService;
import com.nyam.everyday.web.badge.dto.AssignBadgeRequestDto;
import com.nyam.everyday.web.badge.dto.BadgeCreateRequestDto;
import com.nyam.everyday.web.badge.dto.BadgeResponseDto;
import com.nyam.everyday.web.badge.dto.BadgeOwnershipDto;
import com.nyam.everyday.web.badge.mapper.BadgeMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final MemberBadgeStatusRepository memberBadgeStatusRepository;
    private final MemberRepository memberRepository;
    private final ScoreLogService scoreLogService;

    private final AwsS3Service awsS3Service;
    private final BadgeMapper badgeMapper;

    /**
     * 새로운 뱃지를 생성합니다.
     */
    @Transactional
    public BadgeResponseDto createBadge(BadgeCreateRequestDto badgeDto, MultipartFile badgeImageFile) {
        Badge badge = new Badge();
        badge.setName(badgeDto.getName());
        badge.setDescription(badgeDto.getDescription());
        if (badgeDto.getBadgeType() != null) {
            badge.setBadgeType(badgeDto.getBadgeType());
        }

        if (badgeImageFile!= null
            && !badgeImageFile.getOriginalFilename().isBlank()
            && badgeImageFile.getOriginalFilename().contains(".")) {
            AwsS3Response newS3Url = awsS3Service.uploadFile(badgeImageFile);
            badge.setBadgeImage(newS3Url.getUrl());
            log.info("badgeImageFile.getOriginalFilename() : {ß}" , badgeImageFile.getOriginalFilename());
            log.info("badgeImageFile is MultipartFile : {}" , newS3Url.getUrl());
        } else {
            badge.setBadgeImage(DEFAULT_BADGE_IMAGE.getValue());
            log.info("badgeImageFile is null");
        }
        badgeRepository.save(badge);

        return badgeMapper.toDto(badge);
    }

    /**
     * 회원에게 뱃지를 부여하고, 뱃지 타입에 따라 점수를 지급합니다.
     */
    @Transactional
    public void assignBadgeToMember(Long memberId, AssignBadgeRequestDto requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "memberId : " + memberId + "에 해당하는 사용자가 없습니다."));
        Badge badge = badgeRepository.findById(requestDto.getBadgeId())
                .orElseThrow(() -> new BaseException(ErrorCode.BADGE_NOT_FOUND));

        // 이미 뱃지를 가지고 있는지 확인
        memberBadgeStatusRepository.findByMemberAndBadge(member, badge).ifPresent(mb -> {
            throw new BaseException(ErrorCode.ALREADY_ASSIGN_BADGE);
        });

        MemberBadgeStatus memberBadgeStatus = new MemberBadgeStatus(member, badge);
        memberBadgeStatusRepository.save(memberBadgeStatus);

        // 뱃지 타입에 연결된 점수 가져오기
        int score = badge.getBadgeType().getScore();

        // 점수가 0보다 클 경우에만 ScoreLog 생성
        if (score > 0) {
            scoreLogService.createScoreLog(member, (long) score, SourceType.BADGE_REWARD);
        }
    }



    /** 현재 로그인한 사용자가 보유한 뱃지 여부 isOwned 가 표시된 badge 페이징 호출 */
    public Page<BadgeOwnershipDto> getBadgeListWithOwnership(Pageable pageable, Long currentUserId) {
        // 1. badge 모든 목록 호출
        Page<Badge> badgePage = badgeRepository.findAll(pageable);
        List<Long> badgeIds = badgePage.getContent().stream().map(Badge::getId).toList();

        // 2. 로그인한 사용자가 보유하고 있는 뱃지 정보 추가
        Map<Long, LocalDateTime> ownedMap = memberBadgeStatusRepository
            .findOwnedBadgeProjections(currentUserId, badgeIds).stream()
            .collect(Collectors.toMap(OwnedBadgeDto::getBadgeId, OwnedBadgeDto::getAcquiredAt));

        return badgePage.map(b ->
            new BadgeOwnershipDto(b,
                ownedMap.containsKey(b.getId()),
                ownedMap.get(b.getId())));
    }


    /**
     * 뱃지를 삭제합니다.
     * 해당 뱃지와 연관된 모든 MemberBadgeStatus 레코드도 함께 삭제됩니다.
     */
    @Transactional
    public void deleteBadge(Long badgeId) {
        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("Badge not found with id: " + badgeId));

        // 해당 뱃지와 연관된 모든 MemberBadgeStatus 레코드를 먼저 삭제
        List<MemberBadgeStatus> memberBadgeStatuses = badge.getMemberBadgeStatuses();
        if (!memberBadgeStatuses.isEmpty()) {
            memberBadgeStatusRepository.deleteAll(memberBadgeStatuses);
        }
        badgeRepository.delete(badge);
    }
    
    
}
