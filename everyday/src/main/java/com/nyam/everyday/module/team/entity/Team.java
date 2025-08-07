package com.nyam.everyday.module.team.entity;

import com.nyam.everyday.common.entity.BaseEntity;
import com.nyam.everyday.module.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 생성된 그룹에 대한 entity입니다
 *
 * @author : 이지은
 * @fileName : Team
 * @since : 25. 8. 4.
 */

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "team")
public class Team extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "team_id")
    private Long teamId;

    @Column(name="team_title", nullable = false)
    private String teamTitle;

    @Column(name = "team_description")
    private String teamDescription;

    @Column(name = "team_img")
    private String teamImg;

    @Column(name="team_current_members")
    private int teamCurrentMembers = 1;

    @Column(name="team_max_members")
    private int teamMaxMembers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member owner;
}