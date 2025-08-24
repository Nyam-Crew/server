package com.nyam.everyday.module.team.service;

import com.nyam.everyday.common.aws.s3.service.AwsS3Service;
import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.common.exception.ErrorCode;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.ranking.repository.TeamGlobalRankingRepository;
import com.nyam.everyday.module.team.enums.ParticipationStatus;
import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.module.team.enums.TeamRole;
import com.nyam.everyday.module.team.repository.*;
import com.nyam.everyday.web.team.dto.*;
import com.nyam.everyday.web.team.mapper.TeamMapper;
import com.nyam.everyday.web.team.mapper.TeamMemberStatusMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author : 이지은
 * @fileName : TeamService
 * @since : 25. 8. 5.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final TeamMemberStatusRepository teamMemberStatusRepository;
    private final TeamActivityFeedRepository teamActivityFeedRepository;
    private final TeamGlobalRankingRepository teamGlobalRankingRepository;
    private final TeamNoticeRepository teamNoticeRepository;
    private final TeamNotificationRepository teamNotificationRepository;
    private final TeamRankingHistoryRepository teamRankingHistoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final TeamMapper teamMapper;
    private final TeamMemberStatusMapper teamMemberStatusMapper;
    private final AwsS3Service awsS3Service;
    // private final RedisRankingService redisRankingService;
    // private final ChatService chatService;

    @Transactional
    public TeamDto createTeam(TeamDto dto,/* MultipartFile imageFile,*/ Long memberId) {
        //memberId에 대한 유효성 검사
        Member owner = memberRepository.findById(memberId).orElseThrow(()
                -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

//        String imageUrl = null;
//        if (imageFile != null && !imageFile.isEmpty()) {
//            AwsS3Response response = awsS3Service.uploadFile(imageFile);
//            imageUrl = response.getUrl();
//            dto.setTeamImg(imageUrl); // 업로드된 이미지 URL DTO에 주입
//        }

        //Mapstruct builder
        Team team = teamMapper.toEntity(dto, owner); // DTO → Entity 변환

        teamRepository.save(team);

        registerLeader(team, owner);

        return teamMapper.toDto(team);
    }

    @Transactional
    public void registerLeader(Team team, Member owner) {
        TeamMemberStatusDto leaderDto = TeamMemberStatusDto.builder()
                .status(ParticipationStatus.APPROVED)
                .teamRole(TeamRole.LEADER)
                .build();

        TeamMemberStatus leaderStatus = teamMemberStatusMapper.toEntity(leaderDto, team, owner);
        teamMemberStatusRepository.save(leaderStatus);
    }

    @Transactional
    public Page<TeamDto> getTeamList(String keyword, String sortBy, boolean availableOnly, Pageable pageable) {

        // 1. 정렬(Sort) 조건 동적으로 생성하기
        Sort sort;
        switch (sortBy.toLowerCase()) {
            case "members":
                sort = Sort.by(Sort.Direction.DESC, "teamCurrentMembers"); // Team 엔티티에 memberCount 필드가 있다고 가정
                break;
            case "name":
                sort = Sort.by(Sort.Direction.ASC, "teamTitle");
                break;
            case "latest":
            default:
                sort = Sort.by(Sort.Direction.DESC, "createdDate");
                break;
        }

        // 2. 기존 Pageable 객체에 새로운 정렬 조건을 적용하여 새로운 Pageable 객체 생성
        Pageable newPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // 3. Specification을 사용하여 동적 검색 조건 생성
        Specification<Team> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 조건 1: keyword가 있으면 teamTitle로 검색 (like 검색)
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("teamTitle")), "%" + keyword.toLowerCase() + "%"));
            }

            // 조건 2: availableOnly가 true이면 '참가 가능' 그룹만 필터링
            // (현재 인원 < 최대 인원)
            if (availableOnly) {
                predicates.add(criteriaBuilder.lessThan(root.get("teamCurrentMembers"), root.get("maxMembers")));
            }

            // 모든 조건을 AND로 연결하여 반환
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 4. Specification과 새로운 Pageable 객체로 데이터 조회
        Page<Team> teams = teamRepository.findAll(spec, newPageable);

        return teams.map(teamMapper::toDto);
    }

    @Transactional
    public TeamDetailDto getTeam(Long teamId, Long memberId) {
        // 1. 그룹 기본 정보 조회 (기존과 동일)
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BaseException(ErrorCode.GROUP_NOT_FOUND));

        // 2. 현재 로그인한 사용자의 상태 및 역할 조회 (기존과 동일)
        Optional<TeamMemberStatus> memberStatusOpt =
                teamMemberStatusRepository.findByTeam_TeamIdAndMember_MemberId(teamId, memberId);

        ParticipationStatus participationStatus = memberStatusOpt
                .map(TeamMemberStatus::getStatus)
                .orElse(null);

        TeamRole teamRole = memberStatusOpt
                .map(TeamMemberStatus::getTeamRole)
                .orElse(null);

        // 3. [추가] 그룹의 리더 닉네임 조회
        // findAllBy... 메소드는 List를 반환하므로, stream().findFirst()로 첫 번째 멤버를 찾습니다.
        String leaderNickname = teamMemberStatusRepository.findAllByTeam_TeamIdAndTeamRole(teamId, TeamRole.LEADER) // 실제 TeamRole Enum 값 사용
                .stream()
                .findFirst() // 리더는 1명이므로 첫 번째 멤버를 가져옴
                .map(status -> status.getMember().getNickname()) // TeamMemberStatus -> Member -> Nickname 순으로 가져옴
                .orElse("리더 정보 없음"); // 리더가 없는 예외적인 경우를 대비한 기본값

        // 4. [추가] 그룹의 부리더 닉네임 조회
        String subLeaderNickname = teamMemberStatusRepository.findAllByTeam_TeamIdAndTeamRole(teamId, TeamRole.SUBLEADER) // 실제 부리더 Enum 값 사용
                .stream()
                .findFirst() // 화면에 부리더를 1명만 표시하므로 첫 번째 멤버를 가져옴
                .map(status -> status.getMember().getNickname())
                .orElse(null); // 부리더는 없을 수 있으므로 null로 처리

        // 5. [수정] 모든 정보를 Mapper로 전달하여 DTO 생성
        // teamMapper의 toDetailDto 메소드도 이 파라미터들을 모두 받도록 수정해야 합니다.
        return teamMapper.toDetailDto(team, participationStatus, teamRole, leaderNickname, subLeaderNickname);
    }

    @Transactional
    public void requestToJoin(Long teamId, Long memberId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BaseException(ErrorCode.GROUP_NOT_FOUND));

        Optional<TeamMemberStatus> existing = teamMemberStatusRepository
                .findByTeam_TeamIdAndMember_MemberId(teamId, memberId);

        if (existing.isPresent()) {
            TeamMemberStatus status = existing.get();
            if (status.getStatus() == ParticipationStatus.APPROVED) {
                throw new BaseException(ErrorCode.ALREADY_JOINED_GROUP);
            } else if (status.getStatus() == ParticipationStatus.PENDING) {
                throw new BaseException(ErrorCode.ALREADY_EXIST_JOIN);
            }
        }

        if (team.getTeamCurrentMembers() >= team.getTeamMaxMembers()) {
            throw new BaseException(ErrorCode.TEAM_CAPACITY_FULL);
        }

        TeamMemberStatus request = TeamMemberStatus.builder()
                .team(team)
                .member(memberRepository.findById(memberId)
                        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND)))
                .status(ParticipationStatus.PENDING)
                .teamRole(TeamRole.MEMBER)
                .build();

        teamMemberStatusRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<TeamMemberStatusDto> getJoinRequestMembers(Long teamId, Long requesterId) {
        TeamMemberStatus requesterStatus = teamMemberStatusRepository
                .findByTeam_TeamIdAndMember_MemberId(teamId, requesterId)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCESS_DENIED));

        if (!requesterStatus.getTeamRole().isManager()) {
            throw new BaseException(ErrorCode.ACCESS_DENIED);
        }

        List<TeamMemberStatus> pendingMembers = teamMemberStatusRepository
                .findAllByTeam_TeamIdAndStatus(teamId, ParticipationStatus.PENDING);

        return teamMemberStatusMapper.toStatusDtoList(pendingMembers);
    }

    @Transactional(readOnly = true)
    public List<TeamMemberDTO> getApprovedTeamMembers(Long teamId) {
        List<TeamMemberStatus> approvedMembers =
                teamMemberStatusRepository.findAllWithMemberByTeam_TeamIdAndStatus(
                        teamId, ParticipationStatus.APPROVED
                );

        return teamMemberStatusMapper.toMemberDtoList(approvedMembers);
    }

    @Transactional
    public TeamDetailDto updateTeam(Long teamId, Long memberId, TeamUpdateDto teamUpdateDto) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BaseException(ErrorCode.GROUP_NOT_FOUND));

        TeamMemberStatus rel = teamMemberStatusRepository
                .findByTeam_TeamIdAndMember_MemberId(teamId, memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        // 공통 권한 체크
        verifyCanEditTeam(rel);

        // 비즈니스 유효성
        if (teamUpdateDto.getMaxMembers() != null &&
                teamUpdateDto.getMaxMembers() < team.getTeamCurrentMembers()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST,
                    "maxMembers cannot be less than currentMemberCount");
        }

        // ✅ 도메인 메서드로 부분 업데이트 (setter 불필요)
        team.updateBasicInfo(
                teamUpdateDto.getTeamTitle(),
                teamUpdateDto.getTeamDescription(),
                teamUpdateDto.getMaxMembers()
        );

        // 영속 상태면 save 불필요(원한다면 유지 가능)
        teamRepository.save(team);

        return teamMapper.toDetailDto(team, rel.getStatus(), rel.getTeamRole());
    }


    /** 승인 멤버 + (LEADER or SUBLEADER)만 수정 가능 */
    public void verifyCanEditTeam(TeamMemberStatus rel) {
        if (rel.getStatus() == null || rel.getStatus() != ParticipationStatus.APPROVED) {
            throw new BaseException(ErrorCode.ACCESS_DENIED);
        }
        TeamRole role = rel.getTeamRole();
        if ((role != TeamRole.LEADER && role != TeamRole.SUBLEADER)) {
            throw new BaseException(ErrorCode.ACCESS_DENIED);
        }
    }

    @Transactional
    public void deleteTeamHard(Long teamId, Long actorMemberId, String confirmTeamTitle) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BaseException(ErrorCode.GROUP_NOT_FOUND));

        TeamMemberStatus rel = teamMemberStatusRepository
                .findByTeam_TeamIdAndMember_MemberId(teamId, actorMemberId)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCESS_DENIED));

        if (!rel.getTeamRole().isLeader()) {
            throw new BaseException(ErrorCode.ACCESS_DENIED, "방장만 삭제할 수 있습니다.");
        }
        if (confirmTeamTitle == null || !team.getTeamTitle().equals(confirmTeamTitle)) {
            throw new BaseException(ErrorCode.INVALID_REQUEST, "확인용 팀명이 일치하지 않습니다.");
        }

        // todo.외부 리소스 정리
        //if (team.getTeamImg() != null) awsS3Service.deleteFileByUrl(team.getTeamImg());
        // redisRankingService.evictTeamKeys(teamId);
        // chatService.deleteAllByTeamId(teamId);

        entityManager.flush();
        entityManager.clear();

        teamActivityFeedRepository.deleteByTeamId(teamId);
        teamGlobalRankingRepository.deleteByTeamId(teamId);
        teamRankingHistoryRepository.deleteByTeamId(teamId);
        teamNotificationRepository.deleteByTeamId(teamId);
        teamNoticeRepository.deleteByTeamId(teamId);
        teamMemberStatusRepository.deleteByTeamId(teamId);

        entityManager.flush();
        entityManager.clear();

        // 2) 부모 삭제
        teamRepository.deleteById(teamId);
    }
    private void assertManagedTeam(Team team) {
        if (team != null && !entityManager.contains(team)) {
            log.warn("Non-managed Team injected! Team#{}", team.getTeamId());
        }
    }

}