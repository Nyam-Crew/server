package com.nyam.everyday.module.member.entity;

import com.nyam.everyday.common.entity.BaseEntity;
import com.nyam.everyday.module.auth.entity.Auth;
import com.nyam.everyday.module.badge.entity.MemberBadgeStatus;
import com.nyam.everyday.module.board.entity.Board;
import com.nyam.everyday.module.boardLike.entity.BoardLike;
import com.nyam.everyday.module.bookmark.entity.Bookmark;
import com.nyam.everyday.module.ranking.entity.MemberGlobalRanking;
import com.nyam.everyday.module.ranking.entity.MemberTeamRanking;
import com.nyam.everyday.module.scorelog.entity.ScoreLog;
import com.nyam.everyday.security.core.Role;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "member")
@Builder(toBuilder = true)
public class Member extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Comment("회원 아이디")
  private Long memberId;

  @Comment("소셜 아이디")
  @Column(nullable = false, unique = true)
  private String providerId;

  @Comment("닉네임")
  @Builder.Default
  @Column(nullable = false)
  private String nickname = "";

  @Comment("이메일")
  @Column
  private String email;

  @Comment("회원 사진")
  @Builder.Default
  @Column
  private String memberImg = "";

  @Comment("성별")
  @Builder.Default
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Gender gender = Gender.U;

  @Comment("키")
  @Builder.Default
  @Column
  private BigDecimal height = BigDecimal.ZERO;

  @Comment("체중")
  @Builder.Default
  @Column
  private BigDecimal weight = BigDecimal.ZERO;

  @Comment("목표체중")
  @Builder.Default
  @Column
  private BigDecimal targetWeight = BigDecimal.ZERO;

  @Comment("나이")
  @Builder.Default
  @Column
  private int age = 0;

  @Comment("권한")
  @Builder.Default
  @Enumerated(EnumType.STRING)
  private Role role = Role.ROLE_USER;


  @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private Auth auth;


  @Comment("회원상태")
  @Column(nullable = false)
  @Builder.Default
  @Enumerated(EnumType.STRING)
  private Status memberStatus = Status.ACTIVATED;

  @Comment("활동레벨")
  @Column(nullable = false)
  @Builder.Default
  @Enumerated(EnumType.STRING)
  private ActivityLevel activityLevel = ActivityLevel.LIGHT;


  @Comment("마지막 로그인 날짜")
  @Column
  private LocalDateTime lastLoginDate;

  @Comment("연속 로그인 횟수")
  @Builder.Default
  @Column(nullable = false)
  private int consecutiveLoginDays = 0;


  @Builder.Default
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Board> boards = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Bookmark> bookmarks = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<MemberBadgeStatus> memberBadgeStatuses = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ScoreLog> scoreLogs = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<BoardLike> boardLikes = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<MemberGlobalRanking> memberGlobalRanking = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<MemberTeamRanking> memberTeamRanking = new ArrayList<>();


}
