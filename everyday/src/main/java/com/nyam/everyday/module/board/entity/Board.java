package com.nyam.everyday.module.board.entity;

import com.nyam.everyday.module.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Board {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Comment("아이디")
  private Long board_id;

  @Comment("회원 아이디")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member_id;

  @Comment("게시글명")
  @Column(nullable = false)
  private String board_title;

  @Comment("게시글 내용")
  @Column(nullable = false)
  private String board_comment;

  @Comment("조회수")
  @Column(nullable = false)
  private Long view_count;

  @Comment("좋아요 수")
  @Column(nullable = false)
  private Long like_count;


  @Column(nullable = false)
  private Long comment_count;

  @Column(nullable = false)
  private String board_type;

  @Column(nullable = false)
  private LocalDateTime created_date;

  @Column(nullable = false)
  private LocalDateTime modified_date;


}
