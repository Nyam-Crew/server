package com.nyam.everyday.module.member.repository;

import com.nyam.everyday.module.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {

  Optional<Member> findByMemberId(Long memberId);

  Optional<Member> findByProviderId(String providerId);
}
