package com.nyam.everyday.module.board.entity;

import com.nyam.everyday.common.entity.BaseEntity;
import com.nyam.everyday.module.boardComment.entity.BoardComment;
import com.nyam.everyday.module.boardLike.entity.BoardLike;
import com.nyam.everyday.module.bookmark.entity.Bookmark;
import com.nyam.everyday.module.member.entity.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Comment;

@Entity
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class Board extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Comment("아이디")
  private Long boardId;

  @Comment("회원 아이디")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Comment("게시글명")
  @Column(nullable = false)
  private String boardTitle;

  @Comment("게시글 내용")
  @Column(nullable = false)
  private String boardContent;

  @Comment("조회수")
  @Column(nullable = false)
  private Long viewCount = 0L;

  @Comment("좋아요 수")
  @Column(nullable = false)
  private Long likeCount = 0L;

  @Comment("댓글 수")
  @Column(nullable = false)
  @Builder.Default
  private Long commentCount = 0L;

  @Column(nullable = false)
  private String boardType;

  @Builder.Default
  @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Bookmark> bookmarks = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<BoardComment> boardComments = new ArrayList<>();


  @Builder.Default
  @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<BoardLike> boardLikes = new ArrayList<>();

  public void increaseCommentCount() {
    this.commentCount++;
  }

  public void increaseViewCount() {this.viewCount++;}

  public void updateLikeCount(long likeCount) {
    this.likeCount = likeCount;
  }

  public void decreaseCommentCount() {

    // 1.null 예외
    if (this.commentCount == null){
      log.warn("commentCount was null for boardId={}", this.boardId);
      this.commentCount = 0L;
      return;
    }
    if (this.commentCount <= 0L){
      log.debug("commentCount already 0 for boardId={}", this.boardId);
      this.commentCount = 0L;
      return;
    }

    this.commentCount =  this.commentCount - 1L;}

}

