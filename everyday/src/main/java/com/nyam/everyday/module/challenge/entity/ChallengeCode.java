package com.nyam.everyday.module.challenge.entity;

public enum ChallengeCode {

  /// REGULAR_CHALLENGE ///

  // ============ WATER ==============
  WATER_FIRST,                  // 처음으로 물 기록하기
  WATER_1L_30DAYS,              // 물 1L 30일 누적

  // ============ WORKOUT ============
  WORKOUT_STEP_TOTAL_100K,              // 누적 걸음 10만 보

  // ============ MEAL_LOG ===========
  MEAL_LOG_FIRST,                 // 첫 식단 기록
  MEAL_LOG_10_TIMES,              // 식단 기록 10회
  MEAL_LOG_ALL_WEEKEND,           // 주말 모든 식사 기록
  MEAL_LOG_PROTEIN_RDA_3DAYS,        // 단백질 권장량 3일 이상 달성
  MEAL_LOG_PERFECT_ALL_GOALS,        // 퍼펙트 데이
  MEAL_LOG_BALANCED_DAY,            // 균형 잡힌 하루 식단

  // ============ LOGIN ==============
  LOGIN_FIRST,                  // 첫 접속
  LOGIN_30DAYS,             // 30일 이상 접속

  // ============ COMMENT ============
  COMMENT_FIRST,                // 댓글 첫 작성
  COMMENT_50_TIMES,             // 응원 댓글 50개

  // ============ POST ===============
  POST_BEFORE_AFTER_FIRST,      // 비포&애프터 첫 작성
  POST_REPIPE_FIRST,            // RECIPE 첫 등록

  // ============ LIKE ===============
  LIKE_FIRST,                      // 처음 좋아요 누르기
  LIKE_RECEIVE_FIRST,              // 첫 좋아요 받기

  // ============ BOOKMARK ===========
  BOOKMARK_RECIPE_10,             // 레시피 10개 북마크
  BOOKMARK_RECEIVED_5,            // 내 글 5회 북마크

  // ============ TEAM ===============
  TEAM_GOAL_ACHIEVE,            // 그룹 목표 달성
  TEAM_FULL_CAPACITY,           // 그룹 정원 채우기
  TEAM_NOTICE_FIRST,            // 그룹 공지 첫 작성

  // ============ PROFILE ==============
  PROFILE_COMPLETE,             // 프로필 입력 완료
  PROFILE_SET_TARGET_WEIGHT,    // 목표 체중 설정

  // ============ MISSION ==============
  MISSION_FULL_10_TIMES,        // 데일리 미션 100% 10회

  // ============ STAMP ================
  STAMP_FIRST,                  // 처음으로 도장 받기
  STAMP_STREAK_3,               // 도장을 3일 연속 받는다
  STAMP_10_TIMES,               // 도장을 10번 받는다

  // ============ WEIGHT ===============
  WEIGHT_GOAL_50P,              // 목표 체중 50% 달성

  /// EVENT CHALLENGE ///
  RECORD_NEWYEAR_RESOLVE,       // 새해 다짐 기록
  POST_WORKOUT_DONE_15,         // 운동완료 글 15회 작성
  WORKOUT_BURN_1000KCAL,        // 운동으로 1000kcal 소모
  STEP_TOTAL_250K_DEC25,        // 12/1~25 누적 25만 보
  WATER_2L_EVERYDAY_7D,         // 7일 연속 매일 2L
  PROTEIN_20G_EACHMEAL_5D,      // 5일 연속 매끼 20g 단백질
  DIET_CLEAN_WEEKEND,           // 클린이팅 주말 챌린지
  MINDFUL_DIGITAL_DETOX_3D;     // 식사시간 폰 금지 3일
}