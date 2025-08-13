package com.nyam.everyday.module.badge.entity;

import com.nyam.everyday.common.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "badge")
public class Badge extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "badge_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(name = "badge_image", nullable = false)
    private String badgeImage;

    @OneToMany(mappedBy = "badge")
    @Builder.Default
    private List<MemberBadgeStatus> memberBadgeStatuses = new ArrayList<>();
}
