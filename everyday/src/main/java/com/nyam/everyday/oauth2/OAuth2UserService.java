package com.nyam.everyday.oauth2;


import com.nyam.everyday.common.aws.s3.entity.S3DefaultValue;
import com.nyam.everyday.module.auth.entity.Auth;
import com.nyam.everyday.module.auth.repository.AuthRepository;
import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.security.core.CustomUserDetails;
import com.nyam.everyday.security.jwt.JwtTokenProvider;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

  private final AuthRepository authRepository;
  private final MemberRepository memberRepository;
  private final JwtTokenProvider jwtTokenProvider;

  @Value("${jwt.accessTokenExpirationTime}")
  private Long jwtAccessTokenExpirationTime;
  @Value("${jwt.refreshTokenExpirationTime}")
  private Long jwtRefreshTokenExpirationTime;



  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

    OAuth2User oAuth2User = super.loadUser(userRequest);
    String provider = userRequest.getClientRegistration().getRegistrationId();

    Map<String, Object> attributes = oAuth2User.getAttributes();
    String providerId, username, email;

    if ("google".equals(provider)) {

      email = (String) attributes.get("email");
      providerId = email;
      username = (String) attributes.get("name");

    } else if ("kakao".equals(provider)) {

      providerId = attributes.get("id").toString()+"@kakao";   // 고유 id + 구분자
      Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
      email = (String) kakaoAccount.get("email");
      Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
      username = (String) profile.get("nickname");

    } else {
      email = null;
      username = null;
      providerId = null;

    }

    log.info("{} 로그인 확인 providerId = {}", provider, providerId);
    log.info("{} 로그인 확인 username = {}", provider, username);

    Member member = memberRepository.findByProviderId(providerId)
        .orElseGet(() -> {
          Member newMember = new Member();
          newMember.setProviderId(providerId);
          newMember.setEmail(email != null ? email : "");
          if(username != null) newMember.setNickname(username);
          newMember.setMemberImg(S3DefaultValue.DEFAULT_PROFILE_IMAGE.getValue());
          newMember.setLastLoginDate(LocalDateTime.now());
          return memberRepository.save(newMember);
        });
    log.info("[OAuth2UserService] 찾은 유저 : {}  ", member.getProviderId());

    // 시큐리티에서 사용할 인증 객체 생성
    CustomUserDetails customUserDetails = new CustomUserDetails(member);
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(
            customUserDetails,
            Collections.singleton(new SimpleGrantedAuthority(member.getRole().name())));


    String accessToken = jwtTokenProvider.generateToken(authentication,jwtAccessTokenExpirationTime);
    String refreshToken = jwtTokenProvider.generateToken(authentication,jwtRefreshTokenExpirationTime);


    Map<String, Object> customAttributes = new HashMap<>(attributes);
    customAttributes.put("accessToken", accessToken);
    customAttributes.put("refreshToken", refreshToken);
    customAttributes.put("memberId", member.getMemberId());

    Optional<Auth> optionalAuth = authRepository.findByMember(member);
    if (optionalAuth.isPresent()) {
      Auth auth = optionalAuth.get();
      auth.updateAccessToken(accessToken);
      auth.updateRefreshToken(refreshToken);
      authRepository.save(auth);
      member.setAuth(auth);
    } else {
      Auth auth = new Auth(member,refreshToken, accessToken, "Bearer ");
      authRepository.save(auth);
      member.setAuth(auth);
    }

    return new DefaultOAuth2User(
        Collections.singleton(new SimpleGrantedAuthority(member.getRole().name())),
        customAttributes,
        "memberId"
    );
  }
}