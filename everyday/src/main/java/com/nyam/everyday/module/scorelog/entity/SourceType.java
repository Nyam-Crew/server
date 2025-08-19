package com.nyam.everyday.module.scorelog.entity;

/**
 * scoreLogs를 얻을 수 있는 조건 Enum
 */
public enum SourceType {
    MEAL_INPUT,     //식단 기록
    BADGE_REWARD,   //상시 챌린지 클리어
    CHALLENGE_CLEAR,//이벤트 챌린지 클리어
    STAMP,          //도장(데일리미션)
    WATER_INTAKE,   //물
    ATTENDANCE      //출석
}