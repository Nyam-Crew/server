package com.nyam.everyday.common.util;

import com.nyam.everyday.module.member.entity.ActivityLevel;
import com.nyam.everyday.module.member.entity.Gender;
import com.nyam.everyday.module.member.entity.Member;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *  BMI(체질량지수), BMR(기초대사량), TDEE(하루 총에너지 소비량) 계산
 * */
public class HealthCalculator {

  /**
   * Member 객체를 받아 모든 건강 지표를 계산하고 HealthInfo 객체로 반환합니다.
   */
  public static HealthInfo calculate(Member member) {
    BigDecimal weight = member.getWeight();
    BigDecimal height = member.getHeight();
    int age = member.getAge();
    Gender gender = member.getGender();
    ActivityLevel activityLevel = member.getActivityLevel();

    BigDecimal bmi = calculateBmi(weight, height);
    BigDecimal bmr = calculateBmr(weight, height, age, gender);
    BigDecimal tdee = calculateTdee(bmr, activityLevel);

    return new HealthInfo(bmi, bmr, tdee);
  }
  /**
   * BMI(체질량지수)를 계산합니다.
   */
  public static BigDecimal calculateBmi(BigDecimal weight, BigDecimal height) {
    // 키 또는 몸무게 정보가 없으면 계산하지 않고 0을 반환합니다.
    if (height == null || weight == null || height.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }

    // 키(cm)를 미터(m) 단위로 변환
    BigDecimal heightInMeter = height.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
    BigDecimal heightSquared = heightInMeter.multiply(heightInMeter);

    if (heightSquared.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    // BMI 공식 적용: 몸무게(kg) / (키(m) * 키(m))
    return weight.divide(heightSquared, 2, RoundingMode.HALF_UP);
  }


  /**
   * BMR(기초대사량)을 계산합니다.
   */
  public static BigDecimal calculateBmr(BigDecimal weight, BigDecimal height, int age, Gender gender) {
    if (weight == null || height == null || age <= 0) {
      return BigDecimal.ZERO;
    }

    if (gender == Gender.U) {
      return BigDecimal.ZERO;
    }

    BigDecimal userAge = new BigDecimal(age);
    BigDecimal bmrResult;

    if (gender == Gender.M) {
      bmrResult = new BigDecimal("88.362")
          .add(new BigDecimal("13.397").multiply(weight))
          .add(new BigDecimal("4.799").multiply(height))
          .subtract(new BigDecimal("5.677").multiply(userAge));
    } else { // 'F'
      bmrResult = new BigDecimal("447.593")
          .add(new BigDecimal("9.247").multiply(weight))
          .add(new BigDecimal("3.098").multiply(height))
          .subtract(new BigDecimal("4.330").multiply(userAge));
    }
    return bmrResult.setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * TDEE(하루 총에너지 소비량)를 계산합니다.
   *  개인의 활동수준 (activity_level)
   */
  public static BigDecimal calculateTdee(BigDecimal bmr, ActivityLevel activityLevel) {
    if (bmr.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }

    BigDecimal activityMultiplier = BigDecimal.valueOf(activityLevel.getFactor());
    return bmr.multiply(activityMultiplier).setScale(2, RoundingMode.HALF_UP);
  }

  /**
     * 계산된 건강 지표들을 담는 DTO 클래스
     */
    public record HealthInfo(BigDecimal bmi, BigDecimal bmr, BigDecimal tdee) {

  }
}