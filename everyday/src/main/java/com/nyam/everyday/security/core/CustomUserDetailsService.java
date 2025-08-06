package com.nyam.everyday.security.core;

import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;
  @Override
  public UserDetails loadUserByUsername(String providerId) throws UsernameNotFoundException {
    Member member  = memberRepository.findByProviderId(providerId).orElseThrow(
        ()-> new UsernameNotFoundException("해당 유저가 존재하지 않습니다 providerId -> " + providerId));
    return new CustomUserDetails(member);
  }

  public UserDetails loadUserByMemberId(Long memberId) throws UsernameNotFoundException {
    Member member = memberRepository.findByMemberId(memberId).orElseThrow(
        ()-> new UsernameNotFoundException("해당 유저가 존재하지 않습니다 memberId -> " + memberId));
    return new CustomUserDetails(member);
  }

}