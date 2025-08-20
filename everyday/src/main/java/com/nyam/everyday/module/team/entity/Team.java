package com.nyam.everyday.module.team.entity;

import com.nyam.everyday.common.entity.BaseEntity;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.ranking.entity.MemberTeamRanking;
import com.nyam.everyday.module.ranking.entity.TeamGlobalRanking;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
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
    @Builder.Default
    private int teamCurrentMembers = 1;

    @Column(name="team_max_members")
    private int teamMaxMembers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member owner;

    @Builder.Default
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberTeamRanking> memberTeamRankings = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamGlobalRanking> teamGlobalRankings = new ArrayList<>();


    // 이미지 변경용 도메인 메서드
    public void changeImage(String newImageUrl) {
        this.teamImg = newImageUrl;
    }

    // 기본정보 변경용 도메인 메서드 (예: PATCH API)
    public void updateBasicInfo(String title, String description, Integer maxMembers) {
        if (title != null) this.teamTitle = title;
        if (description != null) this.teamDescription = description;
        if (maxMembers != null) this.teamMaxMembers = maxMembers;
    }

    //그룹 참가 승인시 +1
    public void increaseCurrentMembers(int delta) {
        this.teamCurrentMembers += delta;
    }

    //팀나가기 할때 인원수 -1
    public void decreaseCurrentMembers(int delta) {
        this.teamCurrentMembers = Math.max(0, this.teamCurrentMembers - delta);
    }

    // 그룹 방장 변경
    public void changeLeader(Member newLeader) {
        this.owner = newLeader; // 또는 ownerId 세팅
    }
}