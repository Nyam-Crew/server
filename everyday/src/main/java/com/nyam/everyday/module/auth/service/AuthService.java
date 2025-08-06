package com.nyam.everyday.module.auth.service;

import com.nyam.everyday.module.auth.entity.Auth;
import com.nyam.everyday.module.auth.repository.AuthRepository;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.security.core.Role;
import com.nyam.everyday.security.jwt.JwtTokenProvider;
import com.nyam.everyday.web.auth.dto.LoginRequestDto;
import com.nyam.everyday.web.auth.dto.LoginResponseDto;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthRepository authRepository;
  private final MemberRepository memberRepository;
  private final JwtTokenProvider jwtTokenProvider;

  @Value("${jwt.accessTokenExpirationTime}")
  private Long jwtAccessTokenExpirationTime;
  @Value("${jwt.refreshTokenExpirationTime}")
  private Long jwtRefreshTokenExpirationTime;

  @Transactional
  public LoginResponseDto login(LoginRequestDto dto) {
    Optional<Member> optionalMember = memberRepository.findByProviderId(dto.getProviderId());
    Member member;

    if (optionalMember.isPresent()) {
      member = optionalMember.get();
      // 이미 존재하는 사용자 → 로그인 처리 로직 수행
    } else {
      // 회원가입
      member = new Member();
      member.setProviderId(dto.getProviderId());
      member.setRole(Role.ROLE_USER);
      memberRepository.save(member);
    }

    String accessToken = jwtTokenProvider.generateToken(
        new UsernamePasswordAuthenticationToken(new CustomUserDetails(member)
            , ""), jwtAccessTokenExpirationTime);

    String refreshToken = jwtTokenProvider.generateToken(
        new UsernamePasswordAuthenticationToken(new CustomUserDetails(member)
            , ""), jwtRefreshTokenExpirationTime);

    if(authRepository.existsByMember(member)) {
      Auth auth = member.getAuth();
      auth.setRefreshToken(refreshToken);
      auth.setAccessToken(accessToken);
      authRepository.save(auth);

      return new LoginResponseDto(auth);
    }

      // 위에서 DB 사용자 정보가 없으면 아래 새로 생성해서 로그인 처리
      Auth auth = new Auth(member, refreshToken, accessToken,"Bearer");
      authRepository.save(auth);
      return new LoginResponseDto(auth);
  }



  @Transactional
  public String refreshToken (String refreshToken){
    Auth auth = authRepository.findByRefreshToken(refreshToken).orElseThrow(
        ()-> new IllegalArgumentException("해당 REFRESH_TOKEN 을 찾을 수 없습니다. \n refreshToken  = " +refreshToken));

    String newAccessToken = jwtTokenProvider.generateToken(
        new UsernamePasswordAuthenticationToken(
            new CustomUserDetails(auth.getMember()), ""), jwtAccessTokenExpirationTime);

        auth.updateAccessToken(newAccessToken);
        authRepository.save(auth);
        return newAccessToken;
    }


}
