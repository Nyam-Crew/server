package com.nyam.everyday.module.ranking.service;

import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.ranking.repository.MemberGlobalRankingRepository;
import com.nyam.everyday.module.ranking.repository.MemberTeamRankingRepository;
import com.nyam.everyday.module.ranking.repository.TeamGlobalRankingRepository;
import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import com.nyam.everyday.module.team.enums.ParticipationStatus;
import com.nyam.everyday.module.team.repository.TeamMemberStatusRepository;
import com.nyam.everyday.module.team.repository.TeamRepository;
import com.nyam.everyday.web.ranking.dto.RankingDto;
import com.nyam.everyday.web.ranking.dto.TeamRankingDto;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

import java.time.Clock;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.YearMonth;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 랭킹 서비스를 테스트합니다.
 */
@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberStatusRepository teamMemberStatusRepository;

    @Mock
    private MemberGlobalRankingRepository memberGlobalRankingRepository;

    @Mock
    private TeamGlobalRankingRepository teamGlobalRankingRepository;

    @Mock
    private MemberTeamRankingRepository memberTeamRankingRepository;

    @Mock
    private RankingKeys keys;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    // 고정 Clock: 테스트 재현성 보장
    private final Clock clock = Clock.fixed(
        Instant.parse("2025-08-20T12:00:00Z"),  // UTC 기준 고정 시각
        ZoneId.of("Asia/Seoul")                 // 테스트용 타임존
    );

    private RankingService rankingService;

    private Member testMember;
    private Team testTeam;
    private TeamMemberStatus testTeamMemberStatus;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
        lenient().when(keys.zoneId()).thenReturn(ZoneId.of("Asia/Seoul"));

        // 고정 Clock 주입
        rankingService = new RankingService(
            redisTemplate, memberRepository, teamRepository, teamMemberStatusRepository, 
            memberGlobalRankingRepository, teamGlobalRankingRepository, memberTeamRankingRepository, 
            keys, clock
        );

        // Test data setup
        testMember = Member.builder()
            .memberId(1L)
            .nickname("testUser")
            .build();

        testTeam = Team.builder()
            .teamId(1L)
            .teamTitle("testTeam")
            .teamCurrentMembers(5)
            .build();

        testTeamMemberStatus = TeamMemberStatus.builder()
            .member(testMember)
            .team(testTeam)
            .status(ParticipationStatus.APPROVED)
            .build();
    }

    @Test
    @DisplayName("이벤트 시간을 포함하여 멤버 점수를 업데이트한다.")
    void testUpdateMemberScoreWithEventTime() {
        // Given
        Long memberId = 1L;
        long scoreToAdd = 100L;
        ZonedDateTime eventTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        
        when(keys.monthlySuffix(any(ZonedDateTime.class))).thenReturn("2025-08");
        when(keys.weeklySuffix(any(ZonedDateTime.class))).thenReturn("2025-33");
        when(keys.userMonthlyKey(anyString())).thenReturn("user:monthly:2025-08");
        when(keys.interTeamMonthlyKey(anyString())).thenReturn("interteam:monthly:2025-08");
        when(keys.teamScoreSumMonthlyKey(anyString())).thenReturn("teamsum:monthly:2025-08");
        lenient().when(keys.intraTeamWeeklyKey(anyLong(), anyString())).thenReturn("intrateam:weekly:1:2025-33");
        when(keys.activeIntraTeamWeeklyKey(anyString())).thenReturn("active:weekly:2025-33");
        when(keys.teamMemberCountMonthlyHash(anyString())).thenReturn("teamcount:monthly:2025-08");
        
        lenient().when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(teamMemberStatusRepository.getAllByMember_MemberId(memberId))
            .thenReturn(List.of(testTeamMemberStatus));
        
        // Mock multiGet for team member count
        when(hashOperations.multiGet(anyString(), anyList())).thenReturn(List.of("5"));
        
        // Mock executePipelined to return mock results
        doReturn(Arrays.asList(null, null, null, 100.0)).when(redisTemplate).executePipelined(any(SessionCallback.class));

        // When
        rankingService.updateMemberScore(memberId, scoreToAdd, eventTime);

        // Then
        verify(redisTemplate, times(2)).executePipelined(any(SessionCallback.class));
        verify(teamMemberStatusRepository).getAllByMember_MemberId(memberId);
        
        System.out.println("[DEBUG_LOG] updateMemberScore test completed successfully");
    }

    @Test
    @DisplayName("이벤트 시간 없이 멤버 점수를 업데이트한다.")
    void testUpdateMemberScoreWithoutEventTime() {
        // Given
        Long memberId = 1L;
        long scoreToAdd = 50L;
        
        when(keys.monthlySuffix(any(ZonedDateTime.class))).thenReturn("2025-08");
        when(keys.weeklySuffix(any(ZonedDateTime.class))).thenReturn("2025-33");
        when(keys.userMonthlyKey(anyString())).thenReturn("user:monthly:2025-08");
        when(keys.interTeamMonthlyKey(anyString())).thenReturn("interteam:monthly:2025-08");
        when(keys.teamScoreSumMonthlyKey(anyString())).thenReturn("teamsum:monthly:2025-08");
        lenient().when(keys.intraTeamWeeklyKey(anyLong(), anyString())).thenReturn("intrateam:weekly:1:2025-33");
        when(keys.activeIntraTeamWeeklyKey(anyString())).thenReturn("active:weekly:2025-33");
        when(keys.teamMemberCountMonthlyHash(anyString())).thenReturn("teamcount:monthly:2025-08");
        
        lenient().when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(teamMemberStatusRepository.getAllByMember_MemberId(memberId))
            .thenReturn(List.of(testTeamMemberStatus));
        
        // Mock multiGet for team member count
        when(hashOperations.multiGet(anyString(), anyList())).thenReturn(List.of("5"));
        
        // Mock executePipelined to return mock results
        doReturn(Arrays.asList(null, null, null, 100.0)).when(redisTemplate).executePipelined(any(SessionCallback.class));

        // When
        rankingService.updateMemberScore(memberId, scoreToAdd);

        // Then
        verify(redisTemplate, times(2)).executePipelined(any(SessionCallback.class));
        verify(teamMemberStatusRepository).getAllByMember_MemberId(memberId);
        
        System.out.println("[DEBUG_LOG] updateMemberScore without eventTime test completed successfully");
    }

    @Test
    @DisplayName("멤버 점수 업데이트 시 모든 소속 팀에 점수가 반영된다.")
    void testUpdateMemberScore_ShouldReflectInAllTeams() {
        // Given
        Long memberId = 1L;
        long scoreToAdd = 100L;
        ZonedDateTime eventTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        Team team1 = Team.builder().teamId(1L).teamTitle("team1").teamCurrentMembers(3).build();
        Team team2 = Team.builder().teamId(2L).teamTitle("team2").teamCurrentMembers(4).build();

        TeamMemberStatus tms1 = TeamMemberStatus.builder().member(testMember).team(team1).status(ParticipationStatus.APPROVED).build();
        TeamMemberStatus tms2 = TeamMemberStatus.builder().member(testMember).team(team2).status(ParticipationStatus.APPROVED).build();

        when(teamMemberStatusRepository.getAllByMember_MemberId(memberId)).thenReturn(List.of(tms1, tms2));

        String monthlySuffix = "2025-08";
        String weeklySuffix = "2025-33";
        when(keys.monthlySuffix(any(ZonedDateTime.class))).thenReturn(monthlySuffix);
        when(keys.weeklySuffix(any(ZonedDateTime.class))).thenReturn(weeklySuffix);
        when(keys.userMonthlyKey(monthlySuffix)).thenReturn("user:monthly:" + monthlySuffix);
        when(keys.interTeamMonthlyKey(monthlySuffix)).thenReturn("interteam:monthly:" + monthlySuffix);
        when(keys.teamScoreSumMonthlyKey(monthlySuffix)).thenReturn("teamsum:monthly:" + monthlySuffix);
        when(keys.activeIntraTeamWeeklyKey(weeklySuffix)).thenReturn("active:weekly:" + weeklySuffix);
        when(keys.teamMemberCountMonthlyHash(monthlySuffix)).thenReturn("teamcount:monthly:" + monthlySuffix);

        // Mock for team 1
        lenient().when(keys.intraTeamWeeklyKey(1L, weeklySuffix)).thenReturn("intrateam:weekly:1:" + weeklySuffix);

        // Mock for team 2
        lenient().when(keys.intraTeamWeeklyKey(2L, weeklySuffix)).thenReturn("intrateam:weekly:2:" + weeklySuffix);
        
        // Mock multiGet for team member counts (for both teams)
        when(hashOperations.multiGet(anyString(), anyList())).thenReturn(List.of("3", "4"));
        
        // Mock executePipelined to return mock results (for both teams)
        doReturn(Arrays.asList(null, null, null, 100.0, null, null, 200.0)).when(redisTemplate).executePipelined(any(SessionCallback.class));

        // When
        rankingService.updateMemberScore(memberId, scoreToAdd, eventTime);

        // Then
        // Verify that pipelined operations were executed (twice - once for each pipeline stage)
        verify(redisTemplate, times(2)).executePipelined(any(SessionCallback.class));
        verify(teamMemberStatusRepository).getAllByMember_MemberId(memberId);
        verify(redisTemplate).expire("active:weekly:" + weeklySuffix, Duration.ofDays(21));
    }


    @Test
    @DisplayName("존재하지 않는 멤버의 점수를 업데이트한다.")
    void testUpdateMemberScoreWithNonExistentMember() {
        // Given
        Long memberId = 999L;
        long scoreToAdd = 100L;
        
        when(keys.monthlySuffix(any(ZonedDateTime.class))).thenReturn("2025-08");
        when(keys.weeklySuffix(any(ZonedDateTime.class))).thenReturn("2025-33");
        when(keys.userMonthlyKey(anyString())).thenReturn("user:monthly:2025-08");
        
        // Mock empty team member status list (no teams for this member)
        when(teamMemberStatusRepository.getAllByMember_MemberId(memberId))
            .thenReturn(Collections.emptyList());
        
        // Mock multiGet for empty team list
        lenient().when(hashOperations.multiGet(anyString(), anyList())).thenReturn(Collections.emptyList());
        
        // Mock executePipelined to return empty results
        doReturn(Collections.emptyList()).when(redisTemplate).executePipelined(any(SessionCallback.class));

        // When
        rankingService.updateMemberScore(memberId, scoreToAdd);

        // Then
        verify(redisTemplate).executePipelined(any(SessionCallback.class));
        verify(teamMemberStatusRepository).getAllByMember_MemberId(memberId);
        
        System.out.println("[DEBUG_LOG] updateMemberScore with non-existent member test completed successfully");
    }

    @Test
    @DisplayName("상위 랭커 목록을 조회한다.")
    void testGetTopRankers() {
        // Given
        int limit = 10;
        Integer year = 2025;
        Integer month = 8;
        String monthlyKey = "user:monthly:2025-08";
        YearMonth yearMonth = YearMonth.of(year, month);
        
        when(keys.monthlySuffix(yearMonth)).thenReturn("2025-08");
        when(keys.userMonthlyKey("2025-08")).thenReturn(monthlyKey);
        
        Set<TypedTuple<String>> mockTuples = createMockTypedTuples();
        when(zSetOperations.reverseRangeWithScores(monthlyKey, 0, limit - 1)).thenReturn(mockTuples);
        
        when(memberRepository.findAllById(anyList())).thenReturn(List.of(testMember));

        // When
        List<RankingDto> result = rankingService.getTopRankers(limit, year, month);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMemberId()).isEqualTo(1L);
        assertThat(result.get(0).getNickname()).isEqualTo("testUser");
        
        System.out.println("[DEBUG_LOG] getTopRankers test completed successfully");
    }

    @Test
    @DisplayName("팀 간 랭킹을 조회한다.")
    void testGetInterTeamRanking() {
        // Given
        int limit = 5;
        Integer year = 2025;
        Integer month = 8;
        String monthlyKey = "interteam:monthly:2025-08";
        String teamCountKey = "teamcount:monthly:2025-08";
        YearMonth yearMonth = YearMonth.of(year, month);
        
        when(keys.monthlySuffix(yearMonth)).thenReturn("2025-08");
        when(keys.interTeamMonthlyKey("2025-08")).thenReturn(monthlyKey);
        lenient().when(keys.teamMemberCountMonthlyHash("2025-08")).thenReturn(teamCountKey);
        
        Set<TypedTuple<String>> mockTuples = createMockTypedTuples();
        when(zSetOperations.reverseRangeWithScores(monthlyKey, 0, limit - 1)).thenReturn(mockTuples);
        
        when(teamRepository.findAllById(anyList())).thenReturn(List.of(testTeam));
        lenient().when(hashOperations.get(teamCountKey, "1")).thenReturn("5");

        // When
        List<TeamRankingDto> result = rankingService.getInterTeamRanking(limit, year, month);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTeamId()).isEqualTo(1L);
        assertThat(result.get(0).getTeamName()).isEqualTo("testTeam");
        
        System.out.println("[DEBUG_LOG] getInterTeamRanking test completed successfully");
    }

    @Test
    @DisplayName("팀 내 랭킹을 조회한다.")
    void testGetIntraTeamRanking() {
        // Given
        Long teamId = 1L;
        int limit = 10;
        Integer year = 2025;
        Integer week = 33;
        String weeklyKey = "intrateam:weekly:1:2025-33";
        String weeklySuffix = String.format("%d-%02d", year, week);
        
        when(keys.intraTeamWeeklyKey(teamId, weeklySuffix)).thenReturn(weeklyKey);
        
        Set<TypedTuple<String>> mockTuples = createMockTypedTuples();
        when(zSetOperations.reverseRangeWithScores(weeklyKey, 0, limit - 1)).thenReturn(mockTuples);
        
        when(memberRepository.findAllById(anyList())).thenReturn(List.of(testMember));

        // When
        List<RankingDto> result = rankingService.getIntraTeamRanking(teamId, limit, year, week);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMemberId()).isEqualTo(1L);
        
        System.out.println("[DEBUG_LOG] getIntraTeamRanking test completed successfully");
    }

    @Test
    @DisplayName("멤버의 랭킹을 조회한다.")
    void testGetMemberRank() {
        // Given
        Long memberId = 1L;
        Integer year = 2025;
        Integer month = 8;
        String monthlyKey = "user:monthly:2025-08";
        YearMonth yearMonth = YearMonth.of(year, month);
        
        when(keys.monthlySuffix(yearMonth)).thenReturn("2025-08");
        when(keys.userMonthlyKey("2025-08")).thenReturn(monthlyKey);
        when(zSetOperations.score(monthlyKey, memberId.toString())).thenReturn(100.0);
        when(zSetOperations.reverseRank(monthlyKey, memberId.toString())).thenReturn(0L);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));

        // When
        RankingDto result = rankingService.getMemberRank(memberId, year, month);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMemberId()).isEqualTo(1L);
        assertThat(result.getNickname()).isEqualTo("testUser");
        assertThat(result.getTotalScore()).isEqualTo(100.0);
        assertThat(result.getRank()).isEqualTo(1L);
        
        System.out.println("[DEBUG_LOG] getMemberRank test completed successfully");
    }

    @Test
    @DisplayName("랭킹이 없는 멤버를 조회하면 null을 반환한다.")
    void testGetMemberRankNotFound() {
        // Given
        Long memberId = 999L;
        Integer year = 2025;
        Integer month = 8;
        String monthlyKey = "user:monthly:2025-08";
        YearMonth yearMonth = YearMonth.of(year, month);
        
        when(keys.monthlySuffix(yearMonth)).thenReturn("2025-08");
        when(keys.userMonthlyKey("2025-08")).thenReturn(monthlyKey);
        when(zSetOperations.score(monthlyKey, memberId.toString())).thenReturn(null);

        // When
        RankingDto result = rankingService.getMemberRank(memberId, year, month);

        // Then
        assertThat(result).isNull();
        
        System.out.println("[DEBUG_LOG] getMemberRank not found test completed successfully");
    }

    @Test
    @DisplayName("멤버 랭킹을 초기화한다.")
    void testClearMemberRanking() {
        // Given
        Integer year = 2025;
        Integer month = 8;
        String monthlyKey = "user:monthly:2025-08";
        
        when(keys.userMonthlyKey(String.format("%d-%02d", year, month))).thenReturn(monthlyKey);
        when(redisTemplate.delete(monthlyKey)).thenReturn(true);

        // When
        rankingService.clearMemberRanking(year, month);

        // Then
        verify(redisTemplate).delete(monthlyKey);
        
        System.out.println("[DEBUG_LOG] clearMemberRanking test completed successfully");
    }

    @Test
    @DisplayName("팀 간 랭킹을 초기화한다.")
    void testClearInterTeamRanking() {
        // Given
        Integer year = 2025;
        Integer month = 8;
        String monthlyKey = "interteam:monthly:2025-08";
        String teamSumKey = "teamsum:monthly:2025-08";
        String monthlySuffix = String.format("%d-%02d", year, month);
        
        when(keys.interTeamMonthlyKey(monthlySuffix)).thenReturn(monthlyKey);
        when(keys.teamScoreSumMonthlyKey(monthlySuffix)).thenReturn(teamSumKey);
        when(redisTemplate.delete(monthlyKey)).thenReturn(true);
        when(redisTemplate.delete(teamSumKey)).thenReturn(true);

        // When
        rankingService.clearInterTeamRanking(year, month);

        // Then
        verify(redisTemplate).delete(monthlyKey);
        verify(redisTemplate).delete(teamSumKey);
        
        System.out.println("[DEBUG_LOG] clearInterTeamRanking test completed successfully");
    }

    @Test
    @DisplayName("팀 내 랭킹을 초기화한다.")
    void testClearIntraTeamRanking() {
        // Given
        Long teamId = 1L;
        Integer year = 2025;
        Integer week = 33;
        String weeklyKey = "intrateam:weekly:1:2025-33";
        String weeklySuffix = String.format("%d-%02d", year, week);
        
        when(keys.intraTeamWeeklyKey(teamId, weeklySuffix)).thenReturn(weeklyKey);
        when(redisTemplate.delete(weeklyKey)).thenReturn(true);

        // When
        rankingService.clearIntraTeamRanking(teamId, year, week);

        // Then
        verify(redisTemplate).delete(weeklyKey);
        
        System.out.println("[DEBUG_LOG] clearIntraTeamRanking test completed successfully");
    }

    @Test
    @DisplayName("안전하게 점수와 함께 범위를 조회한다.")
    void testRangeWithScoresSafe() throws Exception {
        // Given
        String key = "test:key";
        Set<TypedTuple<String>> mockTuples = createMockTypedTuples();
        
        when(zSetOperations.reverseRangeWithScores(key, 0, 9)).thenReturn(mockTuples);
        
        // When - Use reflection to test private method
        var method = RankingService.class.getDeclaredMethod("rangeWithScoresSafe", String.class, long.class, long.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<TypedTuple<String>> result = (Set<TypedTuple<String>>) method.invoke(rankingService, key, 0L, 9L);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getValue()).isEqualTo("1");
        assertThat(result.iterator().next().getScore()).isEqualTo(100.0);
        
        System.out.println("[DEBUG_LOG] rangeWithScoresSafe test completed successfully");
    }

    @Test
    @DisplayName("null 결과를 포함하여 안전하게 점수와 함께 범위를 조회한다.")
    void testRangeWithScoresSafeWithNullResult() throws Exception {
        // Given
        String key = "test:key";
        
        when(zSetOperations.reverseRangeWithScores(key, 0, 9)).thenReturn(null);
        
        // When - Use reflection to test private method
        var method = RankingService.class.getDeclaredMethod("rangeWithScoresSafe", String.class, long.class, long.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<TypedTuple<String>> result = (Set<TypedTuple<String>>) method.invoke(rankingService, key, 0L, 9L);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        
        System.out.println("[DEBUG_LOG] rangeWithScoresSafe with null result test completed successfully");
    }


    @Test
    @DisplayName("승인된 모든 팀을 조회한다.")
    void testFindAllApprovedTeams() throws Exception {
        // Given
        Long memberId = 1L;
        List<TeamMemberStatus> teamMemberStatuses = List.of(testTeamMemberStatus);
        
        when(teamMemberStatusRepository.getAllByMember_MemberId(memberId)).thenReturn(teamMemberStatuses);
        
        // When - Use reflection to test private method
        var method = RankingService.class.getDeclaredMethod("findAllApprovedTeams", Long.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Team> result = (List<Team>) method.invoke(rankingService, memberId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTeamId()).isEqualTo(1L);
        
        System.out.println("[DEBUG_LOG] findAllApprovedTeams test completed successfully");
    }

    @Test
    @DisplayName("승인되지 않은 상태의 팀을 조회하지 않는다.")
    void testFindAllApprovedTeamsWithNonApprovedStatus() throws Exception {
        // Given
        Long memberId = 1L;
        TeamMemberStatus nonApprovedStatus = TeamMemberStatus.builder()
            .member(testMember)
            .team(testTeam)
            .status(ParticipationStatus.PENDING)
            .build();
        List<TeamMemberStatus> teamMemberStatuses = List.of(nonApprovedStatus);
        
        when(teamMemberStatusRepository.getAllByMember_MemberId(memberId)).thenReturn(teamMemberStatuses);
        
        // When - Use reflection to test private method
        var method = RankingService.class.getDeclaredMethod("findAllApprovedTeams", Long.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Team> result = (List<Team>) method.invoke(rankingService, memberId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        
        System.out.println("[DEBUG_LOG] findAllApprovedTeams with non-approved status test completed successfully");
    }

    @Test
    @DisplayName("팀 멤버 수 스냅샷을 조회하거나 대체 값을 사용한다.")
    void testGetTeamMemberCountSnapshotOrFallback() throws Exception {
        // Given
        String hashKey = "teamcount:monthly:2025-08";
        Long teamId = 1L;
        
        when(hashOperations.get(hashKey, "1")).thenReturn("5");
        
        // When - Use reflection to test private method
        var method = RankingService.class.getDeclaredMethod("getTeamMemberCountSnapshotOrFallback", 
            HashOperations.class, String.class, Long.class);
        method.setAccessible(true);
        int result = (int) method.invoke(rankingService, hashOperations, hashKey, teamId);
        
        // Then
        assertThat(result).isEqualTo(5);
        
        System.out.println("[DEBUG_LOG] getTeamMemberCountSnapshotOrFallback test completed successfully");
    }

    @Test
    @DisplayName("스냅샷이 없는 경우 팀 멤버 수 대체 값을 사용한다.")
    void testGetTeamMemberCountSnapshotOrFallbackWithNoSnapshot() throws Exception {
        // Given
        String hashKey = "teamcount:monthly:2025-08";
        Long teamId = 1L;
        
        when(hashOperations.get(hashKey, "1")).thenReturn(null);
        
        // When - Use reflection to test private method
        var method = RankingService.class.getDeclaredMethod("getTeamMemberCountSnapshotOrFallback", 
            HashOperations.class, String.class, Long.class);
        method.setAccessible(true);
        int result = (int) method.invoke(rankingService, hashOperations, hashKey, teamId);
        
        // Then
        assertThat(result).isEqualTo(1); // Should return 1 as fallback when no snapshot and no current members info
        
        System.out.println("[DEBUG_LOG] getTeamMemberCountSnapshotOrFallback with no snapshot test completed successfully");
    }

    private Set<TypedTuple<String>> createMockTypedTuples() {
        Set<TypedTuple<String>> tuples = new LinkedHashSet<>();
        @SuppressWarnings("unchecked")
        TypedTuple<String> tuple = mock(TypedTuple.class);
        when(tuple.getValue()).thenReturn("1");
        when(tuple.getScore()).thenReturn(100.0);
        tuples.add(tuple);
        return tuples;
    }
}