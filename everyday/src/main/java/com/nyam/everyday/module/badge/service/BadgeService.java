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
import com.nyam.everyday.web.badge.dto.BadgeDto;
import com.nyam.everyday.web.badge.dto.BadgeOwnershipDto;
import com.nyam.everyday.web.badge.mapper.BadgeMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final MemberBadgeStatusRepository memberBadgeStatusRepository;
    private final MemberRepository memberRepository;

    private final AwsS3Service awsS3Service;
    private final BadgeMapper badgeMapper;

    /**
     * 새로운 뱃지를 생성합니다.
     */
    @Transactional
    public BadgeDto createBadge(BadgeDto badgeDto) {
        Badge badge = new Badge();
        badge.setName(badgeDto.getName());
        badge.setDescription(badgeDto.getDescription());
        if (badgeDto.getBadgeImage() != null) {
            AwsS3Response newS3Url = awsS3Service.uploadFile(badgeDto.getBadgeImageFile());
            badge.setBadgeImage(newS3Url.getUrl());
        } else {
            badge.setBadgeImage(DEFAULT_BADGE_IMAGE.getValue());
        }
        badgeRepository.save(badge);

        return badgeMapper.toDto(badge);
    }

    /**
     * 회원에게 뱃지를 부여합니다.
     */
    @Transactional
    public void assignBadgeToMember(Long memberId, Long badgeId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "memberId : " + memberId + "에 해당하는 사용자가 없습니다."));
        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 뱃지가 없습니다."));

        // 이미 뱃지를 가지고 있는지 확인
        memberBadgeStatusRepository.findByMemberAndBadge(member, badge).ifPresent(mb -> {
            throw new IllegalStateException("해당 멤버가 이미 뱃지를 보유하고 있습니다.");
        });

        MemberBadgeStatus memberBadgeStatus = new MemberBadgeStatus(member, badge);
        memberBadgeStatusRepository.save(memberBadgeStatus);
    }



    /** 현재 로그인한 사용자가 보유한 뱃지 여부 isOwned 가 표시된 badge 페이징 호출 */
    public Page<BadgeOwnershipDto> getBadgeListWithOwnership(Pageable pageable, Long currentUserId) {
        Page<Badge> badgePage = badgeRepository.findAll(pageable);
        List<Long> pageIds = badgePage.getContent().stream().map(Badge::getId).toList();

        Map<Long, LocalDateTime> ownedMap = memberBadgeStatusRepository
            .findOwnedBadgeProjections(currentUserId, pageIds).stream()
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
