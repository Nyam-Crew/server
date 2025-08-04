package com.nyam.everyday.module.board.entity;

import com.nyam.everyday.common.entity.BaseEntity;
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
import org.hibernate.annotations.Comment;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
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
  private String boardComment;

  @Comment("조회수")
  @Column(nullable = false)
  private Long viewCount;

  @Comment("좋아요 수")
  @Column(nullable = false)
  private Long likeCount;

  @Comment("댓글 수")
  @Column(nullable = false)
  private Long commentCount;

  @Column(nullable = false)
  private String boardType;

  @Builder.Default
  @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Bookmark> bookmarks = new ArrayList<>();

}
