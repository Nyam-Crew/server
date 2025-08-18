package com.nyam.everyday.module.mission.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_mission",
        uniqueConstraints = @UniqueConstraint(name = "uq_daily", columnNames = {"member_id", "mission_id", "mission_date"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DailyMission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_mission_id")
    private Long dailyMissionId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Column(name = "mission_date", nullable = false)
    private LocalDate missionDate; // yyyy-MM-dd

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "completed_by", nullable = false, length = 16)
    private CompletedBy completedBy = CompletedBy.NONE;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "expire_date", nullable = false)
    private LocalDateTime expireDate;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdDate = now;
        this.modifiedDate = now;
    }

    @PreUpdate
    void onUpdate() { this.modifiedDate = LocalDateTime.now(); }
}