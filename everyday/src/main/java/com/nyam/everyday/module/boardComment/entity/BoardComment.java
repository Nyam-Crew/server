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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardComment extends BaseEntity {
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private BoardComment parent;

  public void setParent(BoardComment parent) {
    this.parent = parent;
  }
  // 현재 소프트삭제 상태인지 판단
  public boolean isSoftDeleted() {
    return DELETED_MSG.equals(this.content); // NPE 방지 위해 상수에서 equals
  }

  // 소프트삭제(마스킹) 수행
  public void markDeleted() {
    if (isSoftDeleted()) return;        // 중복 마스킹 방지
    this.content = DELETED_MSG;         // 내용만 바꾸면 @LastModifiedDate가 자동 갱신
  }

  // 댓글 내용 수정(더티체킹 트리거 전용, 세터 대체)
  public void editContent(String newContent) {
    if (this.content != null && this.content.equals(newContent)) return; // 동일내용이면 스킵
    this.content = newContent;                                           // 내용만 변경(감사필드 자동)
  }
  private static final String DELETED_MSG = "삭제된 댓글입니다"; // 소프트삭제 마스킹 상수(오타 방지)


}
