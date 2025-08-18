package com.nyam.everyday.module.team.entity;

import com.nyam.everyday.common.entity.BaseEntity;
import com.nyam.everyday.module.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
/**
 * 그룹 공지 관련 entity
 *
 * @author : 이지은
 * @fileName : TeamNotice
 * @since : 25. 8. 4.
 */

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "team_notice")
public class TeamNotice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_notice_id")
    private Long teamNoticeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Team team;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 부분 수정(PATCH) */
    public void editPartial(String title, String content) {
        boolean changed = false;

        if (title != null) {
            if (title.isBlank()) throw new IllegalArgumentException("제목은 공백일 수 없습니다.");
            this.title = title;
            changed = true;
        }
        if (content != null) {
            this.content = content;
            changed = true;
        }
        if (!changed) {
            throw new IllegalArgumentException("수정할 필드가 없습니다.");
        }
    }

}