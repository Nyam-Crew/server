package com.nyam.everyday.module.auth.repository;

import com.nyam.everyday.module.auth.entity.Auth;
import com.nyam.everyday.module.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRepository extends JpaRepository<Auth,Long> {

  boolean existsByMember(Member member);

  Optional<Auth> findByRefreshToken(String refreshToken);

  Optional<Auth> findByMember(Member member);
}