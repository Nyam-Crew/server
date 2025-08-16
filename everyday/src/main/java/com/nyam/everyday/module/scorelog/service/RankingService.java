package com.nyam.everyday.module.scorelog.service;

import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.web.ranking.dto.RankingDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class RankingService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;

    private static final String RANKING_KEY_PREFIX = "user_ranking:";

    /**
     * 현재 월의 랭킹에 사용자의 점수를 업데이트(증가)합니다.
     * Redis Sorted Set의 ZINCRBY 명령어를 사용하여 원자적으로 점수를 더합니다.
     *
     * @param memberId   점수를 업데이트할 회원의 ID
     * @param scoreToAdd 추가할 점수
     */
    public void updateMemberScore(Long memberId, Long scoreToAdd) {
        String key = getCurrentRankingKey();
        redisTemplate.opsForZSet().incrementScore(key, memberId.toString(), scoreToAdd);
    }

    /**
     * 특정 월(또는 현재 월)의 상위 랭커 목록을 조회합니다.
     * Redis Sorted Set의 ZREVRANGE 명령어를 사용하여 점수가 높은 순으로 사용자를 가져옵니다.
     *
     * @param limit 조회할 랭커의 수
     * @param year  조회할 년도 (null일 경우 현재 년도)
     * @param month 조회할 월 (null일 경우 현재 월)
     * @return 랭킹 정보(순위, 닉네임, 점수 등)가 담긴 DTO 리스트
     */
    public List<RankingDto> getTopRankers(int limit, Integer year, Integer month) {
        String key = getRankingKey(year, month);
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> typedTuples = zSetOperations.reverseRangeWithScores(key, 0, limit - 1);

        if (typedTuples == null) {
            return List.of();
        }

        long rank = 1;
        List<RankingDto> topRankers = new ArrayList<>();
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
            Long memberId = Long.parseLong(Objects.requireNonNull(tuple.getValue()));
            Member member = memberRepository.findByMemberId(memberId).orElse(null);
            String nickname = (member != null) ? member.getNickname() : "Unknown";
            topRankers.add(RankingDto.builder()
                    .memberId(memberId)
                    .nickname(nickname)
                    .totalScore(tuple.getScore())
                    .rank(rank++)
                    .build());
        }
        return topRankers;
    }

    /**
     * 특정 월(또는 현재 월)의 특정 사용자 순위 및 점수를 조회합니다.
     * Redis Sorted Set의 ZREVRANK (순위)와 ZSCORE (점수) 명령어를 사용합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @param year     조회할 년도 (null일 경우 현재 년도)
     * @param month    조회할 월 (null일 경우 현재 월)
     * @return 사용자의 랭킹 정보(순위, 닉네임, 점수 등)가 담긴 DTO
     */
    public RankingDto getUserRank(Long memberId, Integer year, Integer month) {
        String key = getRankingKey(year, month);
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();

        Double score = zSetOperations.score(key, memberId.toString());
        Long rank = zSetOperations.reverseRank(key, memberId.toString());

        Member member = memberRepository.findByMemberId(memberId).orElse(null);
        String nickname = (member != null) ? member.getNickname() : "Unknown";

        return RankingDto.builder()
                .memberId(memberId)
                .nickname(nickname)
                .totalScore(score)
                .rank(rank != null ? rank + 1 : null) // 0-based rank to 1-based rank
                .build();
    }

    /**
     * 현재 날짜를 기준으로 랭킹용 Redis 키를 생성합니다. (예: "user_ranking:2025-08")
     * @return 현재 월의 랭킹 키
     */
    private String getCurrentRankingKey() {
        return RANKING_KEY_PREFIX + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    /**
     * 주어진 년도와 월을 기준으로 랭킹용 Redis 키를 생성합니다.
     * 년도 또는 월이 null이면 현재 월의 키를 반환합니다.
     * @param year  키를 생성할 년도
     * @param month 키를 생성할 월
     * @return 생성된 랭킹 키
     */
    private String getRankingKey(Integer year, Integer month) {
        if (year != null && month != null) {
            return RANKING_KEY_PREFIX + String.format("%d-%02d", year, month);
        }
        return getCurrentRankingKey();
    }
}
