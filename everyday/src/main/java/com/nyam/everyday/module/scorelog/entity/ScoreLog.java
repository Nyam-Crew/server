package com.nyam.everyday.module.scorelog.entity;

import com.nyam.everyday.common.entity.BaseCreatedEntity;
import com.nyam.everyday.module.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "score_log")
public class ScoreLog extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "score_log_id")
    @Comment("스코어 로그 아이디")
    private Long scoreLogId;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    @Comment("회원")
    private Member member;

    @Column(name = "score_amount", nullable = false)
    @Comment("점수")
    private Long scoreAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    @Comment("점수 출처")
    private SourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(name="meal_type")
    @Comment("점수체크를 위한 식사타입")
    private MealType mealType;

    @Column(name="scored_on")
    @Comment("점수 산정 기준일")
    private LocalDate scoredOn;
}