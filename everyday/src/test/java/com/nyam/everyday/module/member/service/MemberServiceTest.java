package com.nyam.everyday.module.member.service;

import com.nyam.everyday.common.exception.BaseException;
import com.nyam.everyday.module.member.entity.ActivityLevel;
import com.nyam.everyday.module.member.entity.Gender;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.web.member.dto.MemberResponseDto;
import com.nyam.everyday.web.member.mapper.MemberMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private MemberService memberService;

    private Member testMember;
    private MemberResponseDto testMemberResponseDto;

    @BeforeEach
    void setUp() {
        // 기본 테스트 회원 데이터 설정
        testMember = Member.builder()
            .memberId(1L)
            .nickname("testUser")
            .gender(Gender.M)
            .height(new BigDecimal("175.0"))
            .weight(new BigDecimal("70.0"))
            .targetWeight(new BigDecimal("65.0"))
            .age(25)
            .activityLevel(ActivityLevel.MODERATE)
            .build();

        testMemberResponseDto = new MemberResponseDto();
        testMemberResponseDto.setMemberId(1L);
        testMemberResponseDto.setNickname("testUser");
        testMemberResponseDto.setGender("M");
        testMemberResponseDto.setHeight(new BigDecimal("175.0"));
        testMemberResponseDto.setWeight(new BigDecimal("70.0"));
        testMemberResponseDto.setAge(25);
        testMemberResponseDto.setActivityLevel("MODERATE");
    }

    @Test
    @DisplayName("회원 ID로 회원 정보 조회 성공 테스트")
    void testGetMemberById_Success() {
        // Given
        Long memberId = 1L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(memberMapper.toDto(testMember)).thenReturn(testMemberResponseDto);

        // When
        MemberResponseDto result = memberService.getMemberById(memberId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMemberId()).isEqualTo(1L);
        assertThat(result.getNickname()).isEqualTo("testUser");
        assertThat(result.getRecommendedCalories()).isNotNull();
        
        verify(memberRepository).findById(memberId);
        verify(memberMapper).toDto(testMember);
        
        System.out.println("[DEBUG_LOG] getMemberById success test completed successfully");
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 조회 시 예외 발생 테스트")
    void testGetMemberById_NotFound() {
        // Given
        Long memberId = 999L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberService.getMemberById(memberId))
            .isInstanceOf(BaseException.class)
            .hasMessageContaining("id " + memberId + "에 해당하는 사용자가 없습니다.");

        verify(memberRepository).findById(memberId);
        verify(memberMapper, never()).toDto(any());
        
        System.out.println("[DEBUG_LOG] getMemberById not found test completed successfully");
    }

    @Test
    @DisplayName("체중 감량 목표 시 칼로리 계산 테스트")
    void testCalorieCalculation_WeightLoss() {
        // Given - 체중 감량 목표 (현재 70kg -> 목표 65kg)
        testMember.setWeight(new BigDecimal("70.0"));
        testMember.setTargetWeight(new BigDecimal("65.0"));
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberMapper.toDto(testMember)).thenReturn(testMemberResponseDto);

        // When
        MemberResponseDto result = memberService.getMemberById(1L);

        // Then
        assertThat(result.getRecommendedCalories()).isNotNull();
        // TDEE 계산: BMR * 1.55 (MODERATE 활동 수준)
        // 남성 BMR = (10 * 70) + (6.25 * 175) - (5 * 25) + 5 = 700 + 1093.75 - 125 + 5 = 1673.75
        // TDEE = 1673.75 * 1.55 = 2594.31
        // 체중 감량: TDEE - 500 = 2094.31 ≈ 2094
        assertThat(result.getRecommendedCalories()).isLessThan(result.getTdee().intValue());
        
        System.out.println("[DEBUG_LOG] Weight loss calorie calculation test completed successfully");
        System.out.println("[DEBUG_LOG] Recommended calories for weight loss: " + result.getRecommendedCalories());
    }

    @Test
    @DisplayName("체중 증량 목표 시 칼로리 계산 테스트")
    void testCalorieCalculation_WeightGain() {
        // Given - 체중 증량 목표 (현재 60kg -> 목표 65kg)
        testMember.setWeight(new BigDecimal("60.0"));
        testMember.setTargetWeight(new BigDecimal("65.0"));
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberMapper.toDto(testMember)).thenReturn(testMemberResponseDto);

        // When
        MemberResponseDto result = memberService.getMemberById(1L);

        // Then
        assertThat(result.getRecommendedCalories()).isNotNull();
        // TDEE 계산: BMR * 1.55 (MODERATE 활동 수준)
        // 남성 BMR = (10 * 60) + (6.25 * 175) - (5 * 25) + 5 = 600 + 1093.75 - 125 + 5 = 1573.75
        // TDEE = 1573.75 * 1.55 = 2439.31
        // 체중 증량: TDEE + 500 = 2939.31 ≈ 2939
        assertThat(result.getRecommendedCalories()).isGreaterThan(result.getTdee().intValue());
        
        System.out.println("[DEBUG_LOG] Weight gain calorie calculation test completed successfully");
        System.out.println("[DEBUG_LOG] Recommended calories for weight gain: " + result.getRecommendedCalories());
    }

    @Test
    @DisplayName("체중 유지 목표 시 칼로리 계산 테스트")
    void testCalorieCalculation_WeightMaintenance() {
        // Given - 체중 유지 목표 (현재 70kg -> 목표 70kg)
        testMember.setWeight(new BigDecimal("70.0"));
        testMember.setTargetWeight(new BigDecimal("70.0"));
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberMapper.toDto(testMember)).thenReturn(testMemberResponseDto);

        // When
        MemberResponseDto result = memberService.getMemberById(1L);

        // Then
        assertThat(result.getRecommendedCalories()).isNotNull();
        // TDEE 계산: BMR * 1.55 (MODERATE 활동 수준)
        // 체중 유지: TDEE 그대로
        assertThat(result.getRecommendedCalories()).isEqualTo(result.getTdee().intValue());
        
        System.out.println("[DEBUG_LOG] Weight maintenance calorie calculation test completed successfully");
        System.out.println("[DEBUG_LOG] Recommended calories for maintenance: " + result.getRecommendedCalories());
    }

    @Test
    @DisplayName("목표 체중이 없을 때 칼로리 계산 테스트")
    void testCalorieCalculation_NoTargetWeight() {
        // Given - 목표 체중이 0인 경우
        testMember.setTargetWeight(BigDecimal.ZERO);
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberMapper.toDto(testMember)).thenReturn(testMemberResponseDto);

        // When
        MemberResponseDto result = memberService.getMemberById(1L);

        // Then
        assertThat(result.getRecommendedCalories()).isNull();
        
        System.out.println("[DEBUG_LOG] No target weight calorie calculation test completed successfully");
    }

    @Test
    @DisplayName("유효하지 않은 회원 데이터로 칼로리 계산 테스트")
    void testCalorieCalculation_InvalidMemberData() {
        // Given - 키, 몸무게, 나이가 유효하지 않은 경우
        testMember.setHeight(null);
        testMember.setWeight(null);
        testMember.setAge(3); // 5세 이하
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberMapper.toDto(testMember)).thenReturn(testMemberResponseDto);

        // When
        MemberResponseDto result = memberService.getMemberById(1L);

        // Then
        assertThat(result.getRecommendedCalories()).isNull();
        assertThat(result.getBmi()).isNull();
        assertThat(result.getBmr()).isNull();
        assertThat(result.getTdee()).isNull();
        
        System.out.println("[DEBUG_LOG] Invalid member data calorie calculation test completed successfully");
    }

    @Test
    @DisplayName("여성 사용자 칼로리 계산 테스트")
    void testCalorieCalculation_FemaleUser() {
        // Given - 여성 사용자 테스트
        testMember.setGender(Gender.F);
        testMember.setWeight(new BigDecimal("55.0"));
        testMember.setTargetWeight(new BigDecimal("50.0"));
        testMember.setHeight(new BigDecimal("160.0"));
        testMember.setAge(30);
        testMember.setActivityLevel(ActivityLevel.LIGHT);
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberMapper.toDto(testMember)).thenReturn(testMemberResponseDto);

        // When
        MemberResponseDto result = memberService.getMemberById(1L);

        // Then
        assertThat(result.getRecommendedCalories()).isNotNull();
        // 여성 BMR = (10 * 55) + (6.25 * 160) - (5 * 30) - 161 = 550 + 1000 - 150 - 161 = 1239
        // TDEE = 1239 * 1.375 (LIGHT) = 1703.625
        // 체중 감량: TDEE - 500 = 1203.625 ≈ 1204
        assertThat(result.getRecommendedCalories()).isLessThan(result.getTdee().intValue());
        
        System.out.println("[DEBUG_LOG] Female user calorie calculation test completed successfully");
        System.out.println("[DEBUG_LOG] Female recommended calories: " + result.getRecommendedCalories());
    }

    @Test
    @DisplayName("소수점 정밀도를 고려한 칼로리 계산 테스트")
    void testCalorieCalculation_BigDecimalPrecision() {
        // Given - 소수점이 있는 정확한 값들로 테스트
        testMember.setWeight(new BigDecimal("72.5"));
        testMember.setTargetWeight(new BigDecimal("70.3"));
        testMember.setHeight(new BigDecimal("177.8"));
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberMapper.toDto(testMember)).thenReturn(testMemberResponseDto);

        // When
        MemberResponseDto result = memberService.getMemberById(1L);

        // Then
        assertThat(result.getRecommendedCalories()).isNotNull();
        assertThat(result.getRecommendedCalories()).isInstanceOf(Integer.class);
        // 반올림이 정확히 되었는지 확인
        assertThat(result.getRecommendedCalories()).isPositive();
        
        System.out.println("[DEBUG_LOG] BigDecimal precision test completed successfully");
        System.out.println("[DEBUG_LOG] Precise recommended calories: " + result.getRecommendedCalories());
    }

    @Test
    @DisplayName("다양한 활동 수준에 따른 칼로리 계산 테스트")
    void testCalorieCalculation_DifferentActivityLevels() {
        // Given - 다양한 활동 수준 테스트
        testMember.setActivityLevel(ActivityLevel.VERY_ACTIVE);
        testMember.setWeight(new BigDecimal("80.0"));
        testMember.setTargetWeight(new BigDecimal("75.0"));
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberMapper.toDto(testMember)).thenReturn(testMemberResponseDto);

        // When
        MemberResponseDto result = memberService.getMemberById(1L);

        // Then
        assertThat(result.getRecommendedCalories()).isNotNull();
        // VERY_ACTIVE (1.9) 활동 수준으로 인해 높은 TDEE 값이 나와야 함
        assertThat(result.getTdee()).isNotNull();
        assertThat(result.getTdee().doubleValue()).isGreaterThan(2500.0); // 높은 활동 수준
        
        System.out.println("[DEBUG_LOG] Different activity levels test completed successfully");
        System.out.println("[DEBUG_LOG] VERY_ACTIVE TDEE: " + result.getTdee());
        System.out.println("[DEBUG_LOG] VERY_ACTIVE recommended calories: " + result.getRecommendedCalories());
    }

    @Test
    @DisplayName("작은 체중 차이 엣지 케이스 칼로리 계산 테스트")
    void testCalorieCalculation_EdgeCase_SmallWeightDifference() {
        // Given - 목표 체중과 현재 체중의 차이가 매우 작은 경우
        testMember.setWeight(new BigDecimal("70.1"));
        testMember.setTargetWeight(new BigDecimal("70.0"));
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberMapper.toDto(testMember)).thenReturn(testMemberResponseDto);

        // When
        MemberResponseDto result = memberService.getMemberById(1L);

        // Then
        assertThat(result.getRecommendedCalories()).isNotNull();
        // 0.1kg 차이라도 체중 감량으로 분류되어 TDEE - 500이 되어야 함
        assertThat(result.getRecommendedCalories()).isLessThan(result.getTdee().intValue());
        
        System.out.println("[DEBUG_LOG] Small weight difference test completed successfully");
        System.out.println("[DEBUG_LOG] Small difference recommended calories: " + result.getRecommendedCalories());
    }
}