package com.nyam.everyday.module.member.repository;

import com.nyam.everyday.module.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {

  Optional<Member> findByMemberId(Long memberId);

  Optional<Member> findByProviderId(String providerId);

  boolean existsByNickname(String nickname);

  // 회원별 5개 미션을 만들기 위한 전체 회원 목록 조회
  @Query("select m.memberId from Member m")
  Page<Long> findAllMemberIds(Pageable pageable);
}
