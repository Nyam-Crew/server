package com.nyam.everyday.module.meal.service;

import com.nyam.everyday.module.food.entity.Food;
import com.nyam.everyday.module.food.repository.FoodRepository;
import com.nyam.everyday.module.meal.type.MealType;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.scorelog.service.ScoreAwardService;
import com.nyam.everyday.module.summary.entity.MemberDailySummary;
import com.nyam.everyday.module.summary.repository.MemberDailySummaryRepository;
import com.nyam.everyday.module.team.enums.ActivityType;
import com.nyam.everyday.module.team.service.TeamActivityFeedService;
import com.nyam.everyday.module.team.service.TeamMemberService;
import com.nyam.everyday.module.team.util.FeedIds;
import com.nyam.everyday.web.meal.dto.MealDayLiteResponse;
import com.nyam.everyday.web.meal.dto.MealDaySummaryResponseDto;
import com.nyam.everyday.web.meal.dto.MealLogRequestDto;
import com.nyam.everyday.web.meal.dto.MealLogResponseDto;
import com.nyam.everyday.module.meal.entity.MealLog;
import com.nyam.everyday.web.meal.mapper.MealLogMapStruct;
import com.nyam.everyday.module.meal.repository.MealLogRepository;
import com.nyam.everyday.web.team.dto.TeamActivityFeedItem;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealLogService {

    private final MealLogMapStruct mealLogMapStruct;
    private final MealLogRepository mealLogRepository;
    private final FoodRepository foodRepository;
    private final MemberRepository memberRepository;
    private final MemberDailySummaryRepository memberDailySummaryRepository;
    private final MemberDailySummaryRepository summaryRepository;
    private final Clock clock; // Asia/Seoul

    private final TeamMemberService teamMemberService;
    private final TeamActivityFeedService teamActivityFeedService;
    private final ScoreAwardService scoreAwardService;

    /* =========================
       날짜별 기록 조회
       ========================= */
    @Transactional(readOnly = true)
    public List<MealLogResponseDto> getMealLogs(Long memberId, String mealType, String date) {
        LocalDate localDate = LocalDate.parse(date);
        Date sqlDate = java.sql.Date.valueOf(localDate); // JPA 파라미터 맞추기

        return mealLogRepository.findMealLogsWithFoodName(memberId, mealType, sqlDate);
    }

    /* =========================
       식사 기록 추가 + 일일요약 누적
       ========================= */
    @Transactional
    public Long addMealLog(MealLogRequestDto dto) {
        Date selectedDate = dto.getMealLogDate();

        // 1) DTO -> Entity
        MealLog mealLog = mealLogMapStruct.toEntity(dto);

        // 2) 연관 엔티티 로드
        Food food = foodRepository.findById(dto.getFoodId())
                .orElseThrow(() -> new IllegalArgumentException("Food not found. id=" + dto.getFoodId()));
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found. id=" + dto.getMemberId()));

        mealLog.setFood(food);
        mealLog.setMember(member);

        mealLog.setMealLogDate(selectedDate);

        LocalDateTime now = LocalDateTime.now();
        if (mealLog.getCreatedDate() == null) mealLog.setCreatedDate(now);
        if (mealLog.getModifiedDate() == null) mealLog.setModifiedDate(now);

        // 3) 로그 저장
        MealLog saved = mealLogRepository.save(mealLog);

        // 4) 요약 로우 upsert(요약일자는 "오늘")

        MemberDailySummary summary = memberDailySummaryRepository
                .findByMember_MemberIdAndSummaryDate(member.getMemberId(), selectedDate)
                .orElseGet(() -> createNewSummary(member, selectedDate, now));

        // 5) DTO 내 total(=합계) 값을 그대로 누적
        BigDecimal protein      = nz(dto.getProtein());       // g
        BigDecimal carbohydrate = nz(dto.getCarbohydrate());  // g
        BigDecimal fat          = nz(dto.getFat());           // g
        BigDecimal kcalBD       = nz(dto.getIntakeKcal());    // kcal (소수 입력 가능)

        summary.setTotalProtein     ( nz(summary.getTotalProtein()).add(protein) );
        summary.setTotalCarbohydrate( nz(summary.getTotalCarbohydrate()).add(carbohydrate) );
        summary.setTotalFat         ( nz(summary.getTotalFat()).add(fat) );
        // 물 섭취는 식사 로그에 없다면 0 유지 (별도 API에서 증가)
        // 칼로리만 Integer 누적
        BigDecimal addKcal = kcalBD.setScale(1, RoundingMode.HALF_UP);

        summary.setTotalKcal(summary.getTotalKcal().add(addKcal));

        summary.setModifiedDate(now);
        memberDailySummaryRepository.save(summary);

        // ✅ 중앙화된 피드 발행 메서드 호출
        publishMealFeed(saved.getMember().getMemberId(), saved.getMealLogDate(), saved.getMealType());

        // ✅ [신규] 식단 기록 점수 부여 로직 호출
        // member 객체와 저장된 mealType을 전달합니다.
        scoreAwardService.awardMealSlotOnce(member, saved.getMealType());

        return saved.getMealLogId();
    }

    /* =========================
       일부 섭취 총량/칼로리 수정 + 요약 delta 반영
       ========================= */
    @Transactional
    public void updateIntakeAmountAndKcal(Long userId, Long mealLogId,
                                          Integer intakeAmount, Double intakeKcal,
                                          BigDecimal protein, BigDecimal carbohydrate, BigDecimal fat) {
        // 1) 권한/기존 로그
        MealLog log = mealLogRepository.findById(mealLogId)
                .orElseThrow(() -> new IllegalArgumentException("MealLog not found. id=" + mealLogId));
        if (!log.getMember().getMemberId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        // 2) 기존 총량
        BigDecimal oldProtein = nz(log.getProtein());
        BigDecimal oldCarb    = nz(log.getCarbohydrate());
        BigDecimal oldFat     = nz(log.getFat());
        BigDecimal oldKcal    = nz(log.getIntakeKcal());

        // 3) 새 총량(파라미터 null이면 기존값 유지)
        Integer    newIntakeAmount = intakeAmount != null ? intakeAmount : (log.getIntakeAmount() == null ? 0 : log.getIntakeAmount());
        BigDecimal newProtein      = protein      != null ? protein      : oldProtein;
        BigDecimal newCarb         = carbohydrate != null ? carbohydrate : oldCarb;
        BigDecimal newFat          = fat          != null ? fat          : oldFat;
        BigDecimal newKcal         = intakeKcal   != null ? BigDecimal.valueOf(intakeKcal) : oldKcal;

        // 4) delta = 새 - 옛
        BigDecimal dProtein = newProtein.subtract(oldProtein);
        BigDecimal dCarb    = newCarb.subtract(oldCarb);
        BigDecimal dFat     = newFat.subtract(oldFat);
        BigDecimal dKcal    = newKcal.subtract(oldKcal);

        // 5) 요약 로우(해당 로그 날짜)
        Date logDate = log.getMealLogDate();
        LocalDateTime now = LocalDateTime.now();

        MemberDailySummary summary = memberDailySummaryRepository
                .findByMember_MemberIdAndSummaryDate(userId, logDate)
                // 선택: 혹시 과거 데이터로 summary가 없을 수도 있으니 안전하게 upsert
                .orElseGet(() -> createNewSummary(log.getMember(), logDate, now));

        // 6) 요약 누적(=기존 + delta)
        summary.setTotalProtein     ( nz(summary.getTotalProtein()).add(dProtein) );
        summary.setTotalCarbohydrate( nz(summary.getTotalCarbohydrate()).add(dCarb) );
        summary.setTotalFat         ( nz(summary.getTotalFat()).add(dFat) );

        BigDecimal kcalDelta = dKcal.setScale(1, RoundingMode.HALF_UP);
        BigDecimal newTotalKcal = summary.getTotalKcal().add(kcalDelta);

        BigDecimal safeTotal = newTotalKcal.compareTo(BigDecimal.ONE) < 0
                ? BigDecimal.ONE
                : newTotalKcal;

        summary.setTotalKcal(safeTotal);

        summary.setModifiedDate(LocalDateTime.now());

        // 7) 로그 자체 갱신(총량 저장)
        log.setIntakeAmount(newIntakeAmount);
        log.setProtein(newProtein);
        log.setCarbohydrate(newCarb);
        log.setFat(newFat);
        log.setIntakeKcal(newKcal);
        log.setModifiedDate(LocalDateTime.now());

        mealLogRepository.save(log);
        memberDailySummaryRepository.save(summary);

        // ✅ 마지막에 중앙화된 피드 발행 메서드 호출
        publishMealFeed(userId, log.getMealLogDate(), log.getMealType());
    }

    /* =========================
       식사 기록 삭제 + 요약 차감
       ========================= */
    @Transactional
    public void deleteMealLog(Long userId, Long mealLogId) {
        MealLog log = mealLogRepository.findById(mealLogId)
                .orElseThrow(() -> new IllegalArgumentException("MealLog not found. id=" + mealLogId));
        if (!log.getMember().getMemberId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        // ✅ 삭제 전에 피드 갱신에 필요한 정보를 변수에 저장
        Date mealLogDate = log.getMealLogDate();
        MealType mealType = log.getMealType();

        BigDecimal oldProtein = nz(log.getProtein());
        BigDecimal oldCarb    = nz(log.getCarbohydrate());
        BigDecimal oldFat     = nz(log.getFat());
        BigDecimal oldKcal    = nz(log.getIntakeKcal());

        // ✅ 여기서 날짜를 로그에서 읽는다 (컨트롤러가 줄 필요 없음)
        Date date = log.getMealLogDate();

        MemberDailySummary summary = memberDailySummaryRepository
                .findByMember_MemberIdAndSummaryDate(userId, date)
                .orElseThrow(() -> new IllegalStateException("MemberDailySummary not found for user/date"));

        // 누적값에서 해당 로그 총량 차감
        summary.setTotalProtein     ( clampNz(summary.getTotalProtein().subtract(oldProtein)) );
        summary.setTotalCarbohydrate( clampNz(summary.getTotalCarbohydrate().subtract(oldCarb)) );
        summary.setTotalFat         ( clampNz(summary.getTotalFat().subtract(oldFat)) );


        BigDecimal minusKcal = oldKcal.setScale(1, RoundingMode.HALF_UP);
        BigDecimal newTotalKcal = summary.getTotalKcal().subtract(minusKcal);

        // 0 미만 방지
        BigDecimal safeTotal = newTotalKcal.compareTo(BigDecimal.ZERO) < 0
                ? BigDecimal.ZERO
                : newTotalKcal;

        summary.setTotalKcal(safeTotal);
        summary.setModifiedDate(LocalDateTime.now());


        memberDailySummaryRepository.save(summary);
        mealLogRepository.delete(log);

        // ✅ 마지막에 중앙화된 피드 발행 메서드 호출
        publishMealFeed(userId, mealLogDate, mealType);
    }

    // =================================================================
    // ✅ [핵심] 식사 피드를 발행하는 중앙화된 private 메서드
    // =================================================================
    private void publishMealFeed(Long memberId, Date mealLogDate, MealType mealType) {
        // 0. 이 식사를 한 사용자가 속한 팀 ID 목록 조회
        Set<Long> teamIds = teamMemberService.findTeamIdsByMember(memberId);
        if (teamIds == null || teamIds.isEmpty()) {
            return; // 속한 팀이 없으면 아무것도 안함
        }

        // 1. FeedIds 유틸리티를 사용하여 고유한 그룹 ID 생성
        String feedId = FeedIds.mealPeriod(memberId, mealLogDate, mealType);

        // 2. DB에서 해당 식사의 최신 기록 목록을 모두 가져옴
        //    (MealLogRepository에 이 메서드를 추가해야 합니다)
        List<MealLog> currentLogs = mealLogRepository.findByMember_MemberIdAndMealLogDateAndMealTypeOrderByCreatedDateAsc(memberId, mealLogDate, mealType);

        // 3. 기록이 모두 삭제된 경우, 피드도 삭제
        if (currentLogs.isEmpty()) {
            teamActivityFeedService.removeFeedItem(teamIds, feedId);
            return;
        }

        // 4. 최신 정보로 피드 내용 계산
        // 총 칼로리 합산
        BigDecimal totalKcal = currentLogs.stream()
                .map(MealLog::getIntakeKcal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(1, RoundingMode.HALF_UP); // 소수점 한자리로 정리

        // 피드의 대표 생성시각(score)은 '첫 음식'을 기록한 시간으로 고정
        MealLog firstLog = currentLogs.get(0);
        long createdAtMs = firstLog.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Member member = firstLog.getMember(); // 멤버 정보 로드

        // 피드에 표시할 정보 구성
        TeamActivityFeedItem feedItem = TeamActivityFeedItem.builder()
                .feedId(feedId)
                .memberId(memberId)
                .nickname(member.getNickname()) // Member 엔티티에 닉네임 필드가 있다고 가정
                //.profileImageUrl(member.getProfileImageUrl()) // Member 엔티티에 프로필 이미지 URL 필드가 있다고 가정
                .activityType(ActivityType.MEAL)
                .mealPeriod(mealType)
                .kcal(totalKcal) // ✅ 계산된 총 칼로리
                .build();

        // 5. 피드 생성/갱신 서비스 호출
        teamActivityFeedService.addFeedItemToTeams(
                teamIds,
                feedId,
                createdAtMs, // 첫 기록 시간 (ZSET score)
                feedItem,
                Duration.ofDays(3) // TTL
        );
    }

    /* =========================
       헬퍼들
       ========================= */
    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
    private static BigDecimal clampNz(BigDecimal v) {
        if (v == null) return BigDecimal.ZERO;
        return v.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : v;
    }

    private MemberDailySummary createNewSummary(Member member, Date summaryDate, LocalDateTime now) {
        return MemberDailySummary.builder()
                .member(member)
                .summaryDate(summaryDate)
                .weight(member.getWeight())                    // 없으면 null 가능
                .totalProtein(BigDecimal.ZERO)                 // g
                .totalCarbohydrate(BigDecimal.ZERO)            // g
                .totalFat(BigDecimal.ZERO)                     // g
                .totalWater(BigDecimal.ZERO)                   // ml (별도 API에서 업데이트)
                .totalKcal(BigDecimal.ZERO)                                  // kcal (정수 누적)
                .createdDate(now)
                .modifiedDate(now)
                .build();
    }

    @Transactional(readOnly = true)
    public MealDaySummaryResponseDto getDaySummary(Long memberId, Date d) {
        ZoneId zone = clock.getZone();

        // meal_log 조회
        var rows = mealLogRepository.findLiteByMemberAndDate(memberId, d);

        // MealType별 그룹핑
        Map<MealType, List<MealLogRepository.LiteRow>> grouped =
                rows.stream().collect(Collectors.groupingBy(MealLogRepository.LiteRow::getMealType));

        Map<MealType, MealDaySummaryResponseDto.MealSummary> buckets = new EnumMap<>(MealType.class);
        for (MealType mt : MealType.values()) {
            List<MealLogRepository.LiteRow> items = grouped.getOrDefault(mt, List.of());

            BigDecimal total = items.stream()
                    .map(MealLogRepository.LiteRow::getIntakeKcal)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Boolean takeMeal = null;
            if (!items.isEmpty()) {
                boolean allZeroOrSkipped = items.stream().allMatch(r ->
                        (r.getIntakeKcal() == null || r.getIntakeKcal().compareTo(BigDecimal.ZERO) == 0) ||
                                "안 먹음".equals(r.getFoodName()));
                takeMeal = allZeroOrSkipped ? Boolean.FALSE : Boolean.TRUE;
            }

            buckets.put(mt, MealDaySummaryResponseDto.MealSummary.builder()
                    .totalKcal(total)
                    .takeMeal(takeMeal)
                    .build());
        }

        // 물, 체중
        BigDecimal water = null;
        BigDecimal weight = null;
        Optional<MemberDailySummary> opt = summaryRepository.findByMember_MemberIdAndSummaryDate(memberId, d);
        if (opt.isPresent()) {
            var s = opt.get();
            water = s.getTotalWater();
            weight = s.getWeight();
        }

        return MealDaySummaryResponseDto.builder()
                .date(d)    // Date 그대로 내려줌
                .meals(buckets)
                .water(water)
                .weight(weight)
                .build();
    }

    @Transactional(readOnly = true)
    public MealDayLiteResponse getDay(Long memberId, LocalDate date) {
        // DB가 DATE 컬럼이므로 java.util.Date 로 변환
        ZoneId zone = clock.getZone();
        Date d = Date.from(date.atStartOfDay(zone).toInstant());

        // 1) 식사 로그 라이트 조회
        var rows = mealLogRepository.findLiteByMemberAndDate(memberId, d);

        // 2) MealType별 그룹 + 합계
        Map<MealType, List<MealDayLiteResponse.MealItemLite>> grouped =
                rows.stream().collect(Collectors.groupingBy(
                        MealLogRepository.LiteRow::getMealType,
                        Collectors.mapping(r -> MealDayLiteResponse.MealItemLite.builder()
                                        .id(r.getMealLogId())
                                        .foodName(r.getFoodName())
                                        .intakeKcal(r.getIntakeKcal())
                                        .build(),
                                Collectors.toList())
                ));

        Map<MealType, MealDayLiteResponse.MealBucket> buckets = new EnumMap<>(MealType.class);
        for (MealType mt : MealType.values()) {
            List<MealDayLiteResponse.MealItemLite> items = grouped.getOrDefault(mt, List.of());
            BigDecimal total = items.stream()
                    .map(MealDayLiteResponse.MealItemLite::getIntakeKcal)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            buckets.put(mt, MealDayLiteResponse.MealBucket.builder()
                    .totalKcal(total)
                    .items(items)
                    .build());
        }

        // 3) 요약(물, 체중, 총칼로리, 수정시각)
        Integer water = null;
        BigDecimal weight = null;
        BigDecimal dayTotalKcal = null;
        Instant updatedAt = null;

        Optional<MemberDailySummary> opt = summaryRepository
                .findByMember_MemberIdAndSummaryDate(memberId, d);
        if (opt.isPresent()) {
            var s = opt.get();
            water = (s.getTotalWater() == null) ? null : s.getTotalWater().intValue();
            weight = s.getWeight();
            dayTotalKcal = s.getTotalKcal();
            if (s.getModifiedDate() != null) {
                updatedAt = s.getModifiedDate().atZone(zone).toInstant();
            }
        }

        // 4) ETag (간단 해시)
        String etag = String.format("W/\"meal-%d-%s-%d\"",
                memberId, date, updatedAt == null ? 0 : updatedAt.toEpochMilli());

        return MealDayLiteResponse.builder()
                .date(date)
                .meals(buckets)
                .water(water)
                .weight(weight)
                .summaryTotalKcal(dayTotalKcal)
                .meta(MealDayLiteResponse.Meta.builder()
                        .updatedAt(updatedAt)
                        .etag(etag)
                        .build())
                .build();
    }

}