package com.nyam.everyday.module.boardComment.entity;

import com.nyam.everyday.common.entity.BaseEntity;
import com.nyam.everyday.module.board.entity.Board;
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
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardComment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Comment("댓글 ID")
  private Long commentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  @Comment("작성자")
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "board_id", nullable = false)
  @Comment("게시글")
  private Board board;

  @Column(nullable = false,length = 100)
  @Comment("댓글 내용")
  private String content;

  @Column(nullable = false,name = "created_date")
  private LocalDateTime createdDate;



}
