package com.nyam.everyday.module.boardLike.entity;


import com.nyam.everyday.common.entity.BaseCreatedEntity;
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
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BoardLike extends BaseCreatedEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Comment("게시글 좋아요")
  private Long board_like_id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id",  nullable = false)
  @Comment("작성자 아이디")
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "board_id",  nullable = false)
  @Comment("게시글 아이디")
  private Board board;


}
