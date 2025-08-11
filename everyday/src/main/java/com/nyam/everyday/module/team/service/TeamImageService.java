package com.nyam.everyday.module.team.service;

import com.nyam.everyday.common.aws.s3.service.AwsS3Service;
import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.awsS3.dto.AwsS3Response;
import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.module.team.enums.ParticipationStatus;
import com.nyam.everyday.module.team.enums.TeamRole;
import com.nyam.everyday.module.team.repository.TeamMemberStatusRepository;
import com.nyam.everyday.module.team.repository.TeamRepository;
import com.nyam.everyday.web.team.dto.TeamDetailDto;
import com.nyam.everyday.web.team.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * 그룹 이미지 서비스
 *
 * @author : 이지은
 * @fileName : TeamImasgeService
 * @since : 25. 8. 8.
 *
 */
@Service
@RequiredArgsConstructor
public class TeamImageService {

    private final TeamRepository teamRepository;
    private final TeamMemberStatusRepository teamMemberStatusRepository;
    private final AwsS3Service awsS3Service;
    private final TeamMapper teamMapper;
    private final TeamService teamService;

    @Transactional
    public TeamDetailDto updateTeamImage(Long teamId, Long memberId, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "업로드할 파일이 없습니다.");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BaseException(ErrorCode.GROUP_NOT_FOUND));

        TeamMemberStatus rel = teamMemberStatusRepository
                .findByTeam_TeamIdAndMember_MemberId(teamId, memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        teamService.verifyCanEditTeam(rel);

        // 업로드 먼저 → 성공 시 기존 삭제 (AwsS3Service가 처리)
        String oldUrl = team.getTeamImg();
        AwsS3Response res = awsS3Service.replaceFile(oldUrl, image);
        team.changeImage(res.getUrl());

        return teamMapper.toDetailDto(team, rel.getStatus(), rel.getTeamRole());
    }

}