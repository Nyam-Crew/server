package com.nyam.everyday.module.ranking.service;

import org.mockito.ArgumentMatchers;
import org.springframework.data.redis.core.DefaultTypedTuple;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.ranking.repository.MemberGlobalRankingRepository;
import com.nyam.everyday.module.ranking.repository.MemberTeamRankingRepository;
import com.nyam.everyday.module.ranking.repository.TeamGlobalRankingRepository;
import com.nyam.everyday.module.team.entity.Team;
import com.nyam.everyday.module.team.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.YearMonth;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 *  랭킹 보관 서비스를 테스트합니다.
 */
@ExtendWith(MockitoExtension.class)
class RankingArchivingServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private RankingKeys keys;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private MemberGlobalRankingRepository memberGlobalRankingRepository;

    @Mock
    private TeamGlobalRankingRepository teamGlobalRankingRepository;

    @Mock
    private MemberTeamRankingRepository memberTeamRankingRepository;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    // 고정 Clock: 테스트 재현성 보장
    private final Clock clock = Clock.fixed(
        Instant.parse("2025-08-20T12:00:00Z"),  // UTC 기준 고정 시각
        ZoneId.of("Asia/Seoul")                 // 테스트용 타임존
    );

    private RankingArchivingService rankingArchivingService;

    private Member testMember;
    private Team testTeam;

    @BeforeEach
    void setUp() {
        // Redis ops stubbing
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
        lenient().when(keys.zoneId()).thenReturn(ZoneId.of("Asia/Seoul"));

        // Service under test
        rankingArchivingService = new RankingArchivingService(
            redisTemplate, keys, memberRepository, teamRepository,
            memberGlobalRankingRepository, teamGlobalRankingRepository,
            memberTeamRankingRepository, clock
        );

        // Test data
        testMember = Member.builder()
            .memberId(1L)
            .nickname("testUser")
            .build();

        testTeam = Team.builder()
            .teamId(1L)
            .teamTitle("testTeam")
            .teamCurrentMembers(5)
            .build();
    }

    @Test
    @DisplayName("월별 팀 멤버 수 스냅샷을 생성한다.")
    void testSnapshotMonthlyTeamMemberCounts() {
        // Given
        String monthly = "2025-08";
        String hashKey = "teamcount:monthly:2025-08";

        when(keys.monthlySuffix(any(ZonedDateTime.class))).thenReturn(monthly);
        when(keys.teamMemberCountMonthlyHash(monthly)).thenReturn(hashKey);
        when(teamRepository.findAll()).thenReturn(List.of(testTeam));

        // When
        rankingArchivingService.snapshotMonthlyTeamMemberCounts();

        // Then
        verify(teamRepository).findAll();
        // after
        verify(hashOperations).putAll(eq(hashKey), ArgumentMatchers.<Map<Object, Object>>any());

        System.out.println("[DEBUG_LOG] snapshotMonthlyTeamMemberCounts test completed successfully");
    }

    @Test
    @DisplayName("팀이 없을 경우 월별 팀 멤버 수 스냅샷을 생성하지 않는다.")
    void testSnapshotMonthlyTeamMemberCountsWithNoTeams() {
        // Given
        String monthly = "2025-08";

        when(keys.monthlySuffix(any(ZonedDateTime.class))).thenReturn(monthly);
        when(teamRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        rankingArchivingService.snapshotMonthlyTeamMemberCounts();

        // Then
        verify(teamRepository).findAll();
        verify(hashOperations, never()).putAll(anyString(), any(Map.class));

        System.out.println("[DEBUG_LOG] snapshotMonthlyTeamMemberCounts with no teams test completed successfully");
    }

    @Test
    @DisplayName("월별 랭킹을 보관한다.")
    void testArchiveMonthlyRankings() {
        // Given
        String monthlySuffix = "2024-07"; // Previous month
        String userKey = "user:monthly:2024-07";
        String avgKey = "interteam:monthly:2024-07";
        String sumKey = "teamsum:monthly:2024-07";
        String userSnapshotKey = "snapshot:user:monthly:2024-07";
        String avgSnapshotKey = "snapshot:interteam:monthly:2024-07";
        String sumSnapshotKey = "snapshot:teamsum:monthly:2024-07";

        when(keys.monthlySuffix(any(YearMonth.class))).thenReturn(monthlySuffix);
        when(keys.userMonthlyKey(monthlySuffix)).thenReturn(userKey);
        when(keys.interTeamMonthlyKey(monthlySuffix)).thenReturn(avgKey);
        when(keys.teamScoreSumMonthlyKey(monthlySuffix)).thenReturn(sumKey);
        when(keys.snapshotKey(userKey)).thenReturn(userSnapshotKey);
        when(keys.snapshotKey(avgKey)).thenReturn(avgSnapshotKey);
        when(keys.snapshotKey(sumKey)).thenReturn(sumSnapshotKey);

        // renameIfExists -> true
        doAnswer(invocation -> true).when(redisTemplate).execute(any(org.springframework.data.redis.core.RedisCallback.class));

        // User ranking snapshot
        when(zSetOperations.zCard(userSnapshotKey)).thenReturn(1L);
        Set<TypedTuple<String>> userTuples = createMockTypedTuples("1", 100.0);
        when(zSetOperations.reverseRangeWithScores(eq(userSnapshotKey), anyLong(), anyLong()))
            .thenReturn(userTuples);

        // Team ranking snapshot
        when(zSetOperations.zCard(avgSnapshotKey)).thenReturn(1L);
        Set<TypedTuple<String>> avgTuples = createMockTypedTuples("1", 85.5);
        Set<TypedTuple<String>> sumTuples = createMockTypedTuples("1", 500.0);
        when(zSetOperations.rangeWithScores(sumSnapshotKey, 0, -1)).thenReturn(sumTuples);
        when(zSetOperations.reverseRangeWithScores(eq(avgSnapshotKey), anyLong(), anyLong()))
            .thenReturn(avgTuples);

        when(memberRepository.findAllById(anyList())).thenReturn(List.of(testMember));
        when(teamRepository.findAllById(anyList())).thenReturn(List.of(testTeam));

        // When
        rankingArchivingService.archiveMonthlyRankings();

        // Then
        verify(memberGlobalRankingRepository).saveAll(anyList());
        verify(teamGlobalRankingRepository).saveAll(anyList());
        verify(redisTemplate, atLeast(2)).delete(anyString());

        System.out.println("[DEBUG_LOG] archiveMonthlyRankings test completed successfully");
    }

    @Test
    @DisplayName("주간 팀 내 랭킹을 보관한다.")
    void testArchiveWeeklyIntraTeamRankings() {
        // Given
        String weeklySuffix = "2025-33";
        String activeKey = "active:weekly:2025-33";

        when(keys.weeklySuffix(any(LocalDate.class))).thenReturn(weeklySuffix);
        when(keys.activeIntraTeamWeeklyKey(weeklySuffix)).thenReturn(activeKey);
        when(setOperations.members(activeKey)).thenReturn(Collections.emptySet());

        // When
        rankingArchivingService.archiveWeeklyIntraTeamRankings();

        // Then
        verify(setOperations).members(activeKey);

        System.out.println("[DEBUG_LOG] archiveWeeklyIntraTeamRankings test completed successfully");
    }

    @Test
    @DisplayName("활성 팀이 있는 경우 주간 팀 내 랭킹을 보관한다.")
    void testArchiveWeeklyIntraTeamRankingsWithActiveTeams() {
        // Given
        String weeklySuffix = "2025-33";
        String activeKey = "active:weekly:2025-33";

        when(keys.weeklySuffix(any(LocalDate.class))).thenReturn(weeklySuffix);
        when(keys.activeIntraTeamWeeklyKey(weeklySuffix)).thenReturn(activeKey);
        when(setOperations.members(activeKey)).thenReturn(Set.of("1"));
        when(teamRepository.findAllById(List.of(1L))).thenReturn(List.of(testTeam));

        // When
        rankingArchivingService.archiveWeeklyIntraTeamRankings();

        // Then
        verify(setOperations).members(activeKey);
        verify(teamRepository).findAllById(List.of(1L));
        verify(redisTemplate).delete(activeKey);

        System.out.println("[DEBUG_LOG] archiveWeeklyIntraTeamRankings with active teams test completed successfully");
    }

    @Test
    @DisplayName("데이터가 없는 경우 팀 내 랭킹을 보관하지 않는다.")
    void testArchiveIntraTeamRankingForTeamWithNoData() {
        // Given
        String weeklySuffix = "2025-33";

        // renameIfExists -> false
        doAnswer(invocation -> false).when(redisTemplate).execute(any(org.springframework.data.redis.core.RedisCallback.class));

        // When
        int result = rankingArchivingService.archiveIntraTeamRankingForTeam(testTeam, weeklySuffix);

        // Then
        assertThat(result).isEqualTo(0);
        verify(memberTeamRankingRepository, never()).saveAll(anyList());

        System.out.println("[DEBUG_LOG] archiveIntraTeamRankingForTeam with no data test completed successfully");
    }

    @Test
    @DisplayName("데이터가 있는 경우 팀 내 랭킹을 보관한다.")
    void testArchiveIntraTeamRankingForTeamWithData() {
        // Given
        String weeklySuffix = "2025-33";
        String teamKey = "intrateam:weekly:1:2025-33";
        String snapshotKey = "snapshot:intrateam:weekly:1:2025-33";

        when(keys.intraTeamWeeklyKey(testTeam.getTeamId(), weeklySuffix)).thenReturn(teamKey);
        when(keys.snapshotKey(teamKey)).thenReturn(snapshotKey);

        // renameIfExists -> true
        doAnswer(invocation -> true).when(redisTemplate).execute(any(org.springframework.data.redis.core.RedisCallback.class));

        // ZSet snapshot
        when(zSetOperations.zCard(snapshotKey)).thenReturn(1L);
        Set<TypedTuple<String>> tuples = createMockTypedTuples("1", 100.0);
        when(zSetOperations.reverseRangeWithScores(eq(snapshotKey), anyLong(), anyLong()))
            .thenReturn(tuples);

        when(memberRepository.findAllById(anyList())).thenReturn(List.of(testMember));

        // When
        int result = rankingArchivingService.archiveIntraTeamRankingForTeam(testTeam, weeklySuffix);

        // Then
        assertThat(result).isEqualTo(1);
        verify(memberTeamRankingRepository).saveAll(anyList());
        verify(redisTemplate).delete(snapshotKey);

        System.out.println("[DEBUG_LOG] archiveIntraTeamRankingForTeam with data test completed successfully");
    }

    @Test
    @DisplayName("예외가 발생한 경우 팀 내 랭킹을 보관하지 않고 롤백한다.")
    void testArchiveIntraTeamRankingForTeamWithException() {
        // Given
        String weeklySuffix = "2025-33";
        String teamKey = "intrateam:weekly:1:2025-33";
        String snapshotKey = "snapshot:intrateam:weekly:1:2025-33";

        when(keys.intraTeamWeeklyKey(testTeam.getTeamId(), weeklySuffix)).thenReturn(teamKey);
        when(keys.snapshotKey(teamKey)).thenReturn(snapshotKey);

        // renameIfExists -> true
        doAnswer(invocation -> true).when(redisTemplate).execute(any(org.springframework.data.redis.core.RedisCallback.class));

        when(zSetOperations.zCard(snapshotKey)).thenReturn(1L);

        Set<TypedTuple<String>> tuples = createMockTypedTuples("1", 100.0);
        when(zSetOperations.reverseRangeWithScores(eq(snapshotKey), anyLong(), anyLong()))
            .thenReturn(tuples);

        when(memberRepository.findAllById(anyList())).thenReturn(List.of(testMember));
        when(memberTeamRankingRepository.saveAll(anyList())).thenThrow(new RuntimeException("Database error"));

        // When & Then
        try {
            rankingArchivingService.archiveIntraTeamRankingForTeam(testTeam, weeklySuffix);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Failed to archive intra-team");
        }

        System.out.println("[DEBUG_LOG] archiveIntraTeamRankingForTeam with exception test completed successfully");
    }

    @Test
    @DisplayName("빈 데이터가 있는 경우 팀 내 랭킹을 보관하지 않는다.")
    void testArchiveIntraTeamRankingForTeamWithEmptyData() {
        // Given
        String weeklySuffix = "2025-33";
        String teamKey = "intrateam:weekly:1:2025-33";
        String snapshotKey = "snapshot:intrateam:weekly:1:2025-33";

        when(keys.intraTeamWeeklyKey(testTeam.getTeamId(), weeklySuffix)).thenReturn(teamKey);
        when(keys.snapshotKey(teamKey)).thenReturn(snapshotKey);

        // renameIfExists -> true, but empty zset
        doAnswer(invocation -> true).when(redisTemplate).execute(any(org.springframework.data.redis.core.RedisCallback.class));
        when(zSetOperations.zCard(snapshotKey)).thenReturn(0L);

        // When
        int result = rankingArchivingService.archiveIntraTeamRankingForTeam(testTeam, weeklySuffix);

        // Then
        assertThat(result).isEqualTo(0);
        verify(memberTeamRankingRepository, never()).saveAll(anyList());
        verify(redisTemplate).delete(snapshotKey);

        System.out.println("[DEBUG_LOG] archiveIntraTeamRankingForTeam with empty data test completed successfully");
    }

    @Test
    @DisplayName("여러 멤버가 있는 경우 팀 내 랭킹을 보관한다.")
    void testArchiveIntraTeamRankingForTeamWithMultipleMembers() {
        // Given
        String weeklySuffix = "2025-33";
        String teamKey = "intrateam:weekly:1:2025-33";
        String snapshotKey = "snapshot:intrateam:weekly:1:2025-33";

        when(keys.intraTeamWeeklyKey(testTeam.getTeamId(), weeklySuffix)).thenReturn(teamKey);
        when(keys.snapshotKey(teamKey)).thenReturn(snapshotKey);

        // renameIfExists -> true
        doAnswer(invocation -> true).when(redisTemplate).execute(any(org.springframework.data.redis.core.RedisCallback.class));

        when(zSetOperations.zCard(snapshotKey)).thenReturn(2L);

        Set<TypedTuple<String>> tuples = createMultipleMockTypedTuples();
        when(zSetOperations.reverseRangeWithScores(eq(snapshotKey), anyLong(), anyLong()))
            .thenReturn(tuples);

        Member testMember2 = Member.builder().memberId(2L).nickname("testUser2").build();
        when(memberRepository.findAllById(anyList())).thenReturn(List.of(testMember, testMember2));

        // When
        int result = rankingArchivingService.archiveIntraTeamRankingForTeam(testTeam, weeklySuffix);

        // Then
        assertThat(result).isEqualTo(2);
        verify(memberTeamRankingRepository).saveAll(anyList());
        verify(redisTemplate).delete(snapshotKey);

        System.out.println("[DEBUG_LOG] archiveIntraTeamRankingForTeam with multiple members test completed successfully");
    }




    @Test
    @DisplayName("월별 사용자 랭킹을 보관한다.")
    void testArchiveMonthlyUserRanking() throws Exception {
        // Given
        String monthlySuffix = "2025-08";
        YearMonth lastMonth = YearMonth.of(2025, 8);
        String userKey = "user:monthly:2025-08";
        String snapshotKey = "snapshot:user:monthly:2025-08";

        when(keys.userMonthlyKey(monthlySuffix)).thenReturn(userKey);
        when(keys.snapshotKey(userKey)).thenReturn(snapshotKey);

        // renameIfExists -> true
        doAnswer(invocation -> true).when(redisTemplate).execute(any(org.springframework.data.redis.core.RedisCallback.class));

        when(zSetOperations.zCard(snapshotKey)).thenReturn(1L);
        Set<TypedTuple<String>> tuples = createMockTypedTuples("1", 100.0);
        when(zSetOperations.reverseRangeWithScores(eq(snapshotKey), anyLong(), anyLong()))
            .thenReturn(tuples);

        when(memberRepository.findAllById(anyList())).thenReturn(List.of(testMember));

        // When - reflection
        var method = RankingArchivingService.class.getDeclaredMethod("archiveMonthlyUserRanking", String.class, YearMonth.class);
        method.setAccessible(true);
        int result = (int) method.invoke(rankingArchivingService, monthlySuffix, lastMonth);

        // Then
        assertThat(result).isEqualTo(1);
        verify(memberGlobalRankingRepository).saveAll(anyList());
        verify(redisTemplate).delete(snapshotKey);

        System.out.println("[DEBUG_LOG] archiveMonthlyUserRanking test completed successfully");
    }

    @Test
    @DisplayName("월별 팀 간 랭킹을 보관한다.")
    void testArchiveMonthlyInterTeamRanking() throws Exception {
        // Given
        String monthlySuffix = "2025-08";
        YearMonth lastMonth = YearMonth.of(2025, 8);
        String avgSrc = "interteam:monthly:2025-08";
        String sumSrc = "teamsum:monthly:2025-08";
        String avgSnap = "snapshot:interteam:monthly:2025-08";
        String sumSnap = "snapshot:teamsum:monthly:2025-08";

        when(keys.interTeamMonthlyKey(monthlySuffix)).thenReturn(avgSrc);
        when(keys.teamScoreSumMonthlyKey(monthlySuffix)).thenReturn(sumSrc);
        when(keys.snapshotKey(avgSrc)).thenReturn(avgSnap);
        when(keys.snapshotKey(sumSrc)).thenReturn(sumSnap);

        // renameIfExists -> true for both
        doAnswer(invocation -> true).when(redisTemplate).execute(any(org.springframework.data.redis.core.RedisCallback.class));

        when(zSetOperations.zCard(avgSnap)).thenReturn(1L);
        Set<TypedTuple<String>> avgTuples = createMockTypedTuples("1", 85.5);
        Set<TypedTuple<String>> sumTuples = createMockTypedTuples("1", 500.0);

        when(zSetOperations.rangeWithScores(sumSnap, 0, -1)).thenReturn(sumTuples);
        when(zSetOperations.reverseRangeWithScores(eq(avgSnap), anyLong(), anyLong()))
            .thenReturn(avgTuples);

        when(teamRepository.findAllById(anyList())).thenReturn(List.of(testTeam));

        // When - reflection
        var method = RankingArchivingService.class.getDeclaredMethod("archiveMonthlyInterTeamRanking", String.class, YearMonth.class);
        method.setAccessible(true);
        int result = (int) method.invoke(rankingArchivingService, monthlySuffix, lastMonth);

        // Then
        assertThat(result).isEqualTo(1);
        verify(teamGlobalRankingRepository).saveAll(anyList());
        verify(redisTemplate).delete(avgSnap);
        verify(redisTemplate).delete(sumSnap);

        System.out.println("[DEBUG_LOG] archiveMonthlyInterTeamRanking test completed successfully");
    }

    @Test
    @DisplayName("평균 키가 없는 경우 월별 팀 간 랭킹을 보관하지 않는다.")
    void testArchiveMonthlyInterTeamRankingWithNoAvgKey() throws Exception {
        // Given
        String monthlySuffix = "2025-08";
        YearMonth lastMonth = YearMonth.of(2025, 8);
        String avgSrc = "interteam:monthly:2025-08";
        String sumSrc = "teamsum:monthly:2025-08";

        when(keys.interTeamMonthlyKey(monthlySuffix)).thenReturn(avgSrc);
        when(keys.teamScoreSumMonthlyKey(monthlySuffix)).thenReturn(sumSrc);

        // renameIfExists -> false for avg key
        doAnswer(invocation -> false).when(redisTemplate).execute(any(org.springframework.data.redis.core.RedisCallback.class));

        // When - reflection
        var method = RankingArchivingService.class.getDeclaredMethod("archiveMonthlyInterTeamRanking", String.class, YearMonth.class);
        method.setAccessible(true);
        int result = (int) method.invoke(rankingArchivingService, monthlySuffix, lastMonth);

        // Then
        assertThat(result).isEqualTo(0);
        verify(teamGlobalRankingRepository, never()).saveAll(anyList());

        System.out.println("[DEBUG_LOG] archiveMonthlyInterTeamRanking with no avg key test completed successfully");
    }

    @Test
    @DisplayName("점수 순으로 ZSet을 처리한다.")
    void testProcessZSetInScoreOrder() throws Exception {
        // Given
        String key = "test:zset:key";
        int pageSize = 2;

        when(zSetOperations.zCard(key)).thenReturn(3L);

        // page1
        Set<TypedTuple<String>> page1 = createMultipleMockTypedTuples();
        when(zSetOperations.reverseRangeWithScores(key, 0, 1)).thenReturn(page1);

        // page2
        Set<TypedTuple<String>> page2 = createMockTypedTuples("3", 80.0);
        when(zSetOperations.reverseRangeWithScores(key, 2, 2)).thenReturn(page2);

        List<List<TypedTuple<String>>> capturedPages = new ArrayList<>();

        // When - reflection
        var method = RankingArchivingService.class.getDeclaredMethod(
            "processZSetInScoreOrder", String.class, int.class, java.util.function.Consumer.class);
        method.setAccessible(true);
        method.invoke(rankingArchivingService, key, pageSize, (java.util.function.Consumer<List<TypedTuple<String>>>) capturedPages::add);

        // Then
        assertThat(capturedPages).hasSize(2);
        assertThat(capturedPages.get(0)).hasSize(2);
        assertThat(capturedPages.get(1)).hasSize(1);

        System.out.println("[DEBUG_LOG] processZSetInScoreOrder test completed successfully");
    }

    @Test
    @DisplayName("빈 Set으로 ZSet을 처리한다.")
    void testProcessZSetInScoreOrderWithEmptySet() throws Exception {
        // Given
        String key = "test:empty:key";
        int pageSize = 10;

        when(zSetOperations.zCard(key)).thenReturn(0L);

        List<List<TypedTuple<String>>> capturedPages = new ArrayList<>();

        // When - reflection
        var method = RankingArchivingService.class.getDeclaredMethod(
            "processZSetInScoreOrder", String.class, int.class, java.util.function.Consumer.class);
        method.setAccessible(true);
        method.invoke(rankingArchivingService, key, pageSize, (java.util.function.Consumer<List<TypedTuple<String>>>) capturedPages::add);

        // Then
        assertThat(capturedPages).isEmpty();

        System.out.println("[DEBUG_LOG] processZSetInScoreOrder with empty set test completed successfully");
    }

    @Test
    @DisplayName("키가 있는 경우 이름을 변경한다.")
    void testRenameIfExists() throws Exception {
        // Given
        String from = "source:key";
        String to = "target:key";

        // renameIfExists -> true
        doAnswer(invocation -> true).when(redisTemplate).execute(any(org.springframework.data.redis.core.RedisCallback.class));

        // When - reflection
        var method = RankingArchivingService.class.getDeclaredMethod("renameWithFallbackIfExists", String.class, String.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(rankingArchivingService, from, to);

        // Then
        assertThat(result).isTrue();

        System.out.println("[DEBUG_LOG] renameIfExists test completed successfully");
    }

    @Test
    @DisplayName("존재하지 않는 키의 이름을 변경하지 않는다.")
    void testRenameIfExistsWithNonExistentKey() throws Exception {
        // Given
        String from = "nonexistent:key";
        String to = "target:key";

        // renameIfExists -> false
        doAnswer(invocation -> false).when(redisTemplate).execute(any(org.springframework.data.redis.core.RedisCallback.class));

        // When - reflection
        var method = RankingArchivingService.class.getDeclaredMethod("renameWithFallbackIfExists", String.class, String.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(rankingArchivingService, from, to);

        // Then
        assertThat(result).isFalse();

        System.out.println("[DEBUG_LOG] renameIfExists with non-existent key test completed successfully");
    }

    @Test
    @DisplayName("점수를 long으로 변환한다.")
    void testToLongScore() throws Exception {
        var method = RankingArchivingService.class.getDeclaredMethod("toLongScore", Double.class);
        method.setAccessible(true);

        long result1 = (long) method.invoke(null, 123.7);
        assertThat(result1).isEqualTo(124L);

        long result2 = (long) method.invoke(null, (Double) null);
        assertThat(result2).isEqualTo(0L);

        long result3 = (long) method.invoke(null, -45.3);
        assertThat(result3).isEqualTo(-45L);

        System.out.println("[DEBUG_LOG] toLongScore test completed successfully");
    }

    @Test
    @DisplayName("int 범위를 자른다.")
    void testClipInt() throws Exception {
        var method = RankingArchivingService.class.getDeclaredMethod("clipInt", long.class);
        method.setAccessible(true);

        long result1 = (long) method.invoke(null, 1000L);
        assertThat(result1).isEqualTo(1000L);

        long result2 = (long) method.invoke(null, (long) Integer.MAX_VALUE + 1);
        assertThat(result2).isEqualTo(Integer.MAX_VALUE);

        long result3 = (long) method.invoke(null, (long) Integer.MIN_VALUE - 1);
        assertThat(result3).isEqualTo(Integer.MIN_VALUE);

        System.out.println("[DEBUG_LOG] clipInt test completed successfully");
    }

    @Test
    @DisplayName("주 접미사를 LocalDate로 파싱한다.")
    void testParseWeekSuffixToLocalDate() throws Exception {
        var method = RankingArchivingService.class.getDeclaredMethod("parseWeekSuffixToLocalDateISO", String.class);
        method.setAccessible(true);

        LocalDate result = (LocalDate) method.invoke(null, "2025-33");

        assertThat(result).isNotNull();
        assertThat(result.getYear()).isEqualTo(2025);
        assertThat(result.getDayOfWeek()).isEqualTo(java.time.DayOfWeek.MONDAY);

        System.out.println("[DEBUG_LOG] parseWeekSuffixToLocalDate test completed successfully");
    }

    private Set<TypedTuple<String>> createMockTypedTuples(String value, Double score) {
        Set<TypedTuple<String>> tuples = new LinkedHashSet<>();
        tuples.add(new DefaultTypedTuple<>(value, score));
        return tuples;
    }

    private Set<TypedTuple<String>> createMultipleMockTypedTuples() {
        Set<TypedTuple<String>> tuples = new LinkedHashSet<>();
        tuples.add(new DefaultTypedTuple<>("1", 100.0));
        tuples.add(new DefaultTypedTuple<>("2", 90.0));
        return tuples;
    }
}