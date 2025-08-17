package com.nyam.everyday.module.mission.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_mission_stamp",
        uniqueConstraints = @UniqueConstraint(name = "uq_daily_mission_stamp", columnNames = {"member_id", "mission_date"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DailyMissionStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stamp_id")
    private Long stampId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "mission_date", nullable = false)
    private LocalDate missionDate;

    @Column(name = "completed_count", nullable = false)
    private int completedCount;

    @Column(name = "achieved", nullable = false)
    private boolean achieved;

    @Column(name = "issued_date", nullable = false)
    private LocalDateTime issuedDate;

    @PrePersist
    void onCreate() { this.issuedDate = LocalDateTime.now(); }
}