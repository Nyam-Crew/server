package com.nyam.everyday.module.mission.service;

import com.nyam.everyday.module.mission.entity.CompletedBy;
import com.nyam.everyday.module.mission.entity.DailyMission;
import com.nyam.everyday.module.mission.entity.Mission;
import com.nyam.everyday.module.mission.repository.DailyMissionRepository;
import com.nyam.everyday.module.mission.repository.MissionRepository;
import com.nyam.everyday.module.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MissionAssignmentService {

    private final MissionRepository missionRepository;
    private final DailyMissionRepository dailyMissionRepository;
    private final MemberRepository memberRepository; // 배치용
    private final Clock clock;

    private static final int TARGET_PER_DAY = 5;

    /** 🔥 오늘 목록을 조회하되, 없으면 오늘걸 즉시 생성해서 반환 */
    @Transactional // ✅ 쓰기 트랜잭션 보장 (readOnly=false)
    public List<DailyMission> getOrAssignTodayMissions(Long memberId) {
        LocalDate today = LocalDate.now(clock);

        // 미션을 미리 '미션까지 fetch join'으로 읽어오면 Lazy 문제도 예방
        List<DailyMission> todayList =
                dailyMissionRepository.findWithMissionByMemberIdAndMissionDateOrderByDailyMissionIdAsc(memberId, today);

        if (todayList.size() >= TARGET_PER_DAY) {
            return todayList;
        }

        // 부족하면 오늘 것도 5개로 맞춰 생성
        topUpToFiveFor(memberId, today);

        // 생성 후 다시 조회 (fetch join)
        return dailyMissionRepository.findWithMissionByMemberIdAndMissionDateOrderByDailyMissionIdAsc(memberId, today);
    }


    /** write 트랜잭션으로 오늘(D) 생성 후 다시 fetch-join 조회 */
    @Transactional
    protected List<DailyMission> createAndFetch(Long memberId, LocalDate date) {
        topUpToFiveFor(memberId, date);
        return dailyMissionRepository.findWithMissionByMemberIdAndMissionDateOrderByDailyMissionIdAsc(memberId, date);
    }

    /** 00:00 원샷: D-2 이하 정리 + D+1 보정 */
    @Transactional
    public void rolloverAtMidnight() {
        LocalDate today = LocalDate.now(clock);
        LocalDate yesterday = today.minusDays(1);

        // 1) 정리: D-2 이하 삭제
        dailyMissionRepository.deleteByMissionDateBefore(yesterday);

        // 2) 내일(D+1) 보정: 5개 미만이면 채우기
        LocalDate nextDay = today.plusDays(1);
        pageAllMembersAndTopUp(nextDay);
    }

    /** 모든 회원을 페이징 순회하며 targetDate를 5개로 맞춤 */
    private void pageAllMembersAndTopUp(LocalDate targetDate) {
        int page = 0;
        int size = 500; // 환경에 맞게 조정
        Page<Long> p;
        do {
            p = memberRepository.findAllMemberIds(PageRequest.of(page, size));
            for (Long memberId : p.getContent()) {
                topUpToFiveFor(memberId, targetDate);
            }
            page++;
        } while (!p.isLast());
    }

    /** 특정 회원/날짜를 정확히 5개로 맞추기 */
    private void topUpToFiveFor(Long memberId, LocalDate missionDate) {
        long current = dailyMissionRepository.countByMemberIdAndMissionDate(memberId, missionDate);
        if (current >= TARGET_PER_DAY) return;

        List<Mission> activePool = missionRepository.findAllActive();
        if (activePool.isEmpty()) return;

        // 이미 배정된 미션 제외
        Set<Long> assigned = new HashSet<>(dailyMissionRepository.findAssignedMissionIds(memberId, missionDate));

        // 가변 리스트로 수집 (shuffle 안전)
        List<Mission> candidates = new ArrayList<>();
        for (Mission m : activePool) {
            if (!assigned.contains(m.getMissionId())) {
                candidates.add(m);
            }
        }
        if (candidates.isEmpty()) return;

        Collections.shuffle(candidates);

        int need = (int) Math.min(TARGET_PER_DAY - current, candidates.size());
        LocalDateTime expireAt = LocalDateTime.of(missionDate.plusDays(2), LocalTime.of(23, 59, 59));

        List<DailyMission> toCreate = new ArrayList<>(need);
        for (int i = 0; i < need; i++) {
            Mission m = candidates.get(i);
            DailyMission dm = DailyMission.builder()
                    .memberId(memberId)
                    .mission(m)
                    .missionDate(missionDate)
                    .isCompleted(false)
                    .completedBy(CompletedBy.NONE)
                    .expireDate(expireAt)
                    .build();
            toCreate.add(dm);
        }
        dailyMissionRepository.saveAll(toCreate);
    }
}