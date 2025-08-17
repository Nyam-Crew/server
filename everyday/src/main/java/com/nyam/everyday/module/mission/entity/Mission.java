package com.nyam.everyday.module.mission.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mission")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_id")
    private Long missionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MissionCategory category;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MissionType type; // AUTO / MANUAL

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true; // 필드명과 컬럼명 명확히 구분

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @PrePersist
    void onCreate() { this.createdDate = LocalDateTime.now(); }
}