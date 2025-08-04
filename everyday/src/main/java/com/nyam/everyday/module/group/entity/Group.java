package com.nyam.everyday.module.group.entity;

import com.nyam.everyday.module.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;

/**
 * 생성된 그룹에 대한 entity입니다
 *
 * @author : 이지은
 * @fileName : Group
 * @since : 25. 8. 4.
 */

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "group")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "group_id")
    private Long groupId;

    @Column(name="group_title")
    private String groupName;

    @Column(name = "group_description")
    private String groupDescription;

    @Column(name="group_max_members")
    private int groupMaxMembers;

    @CreatedDate
    @Column(name = "created_date")
    private LocalDateTime groupCreatedAt;

    @LastModifiedDate
    @Column(name = "modified_date")
    private LocalDateTime groupModifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member owner;
}