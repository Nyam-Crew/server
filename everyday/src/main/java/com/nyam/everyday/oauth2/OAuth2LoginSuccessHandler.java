package com.nyam.everyday.oauth2;

import com.nyam.everyday.module.member.entity.Member;
import com.nyam.everyday.module.member.repository.MemberRepository;
import com.nyam.everyday.module.member.service.MemberService;
import com.nyam.everyday.module.scorelog.service.ScoreAwardService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final MemberService memberService;
  private final ScoreAwardService scoreAwardService;
  private final MemberRepository memberRepository;

  @Value("${aws.ec2ip.domain}")
  private String awsEc2IP;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication)
      throws IOException {

    DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();

    Map<String, Object> attributes = oAuth2User.getAttributes();

    String accessToken = (String) attributes.get("accessToken");
    String refreshToken = (String) attributes.get("refreshToken");
    String name = (String) attributes.get("name");
    String nickname = (String) attributes.get("nickname");

    log.info("[OAuth2_LOG][Success]" + " 소셜 로그인 시도한 name ={}, nickname ={} ", name, nickname);

    Long memberId;
    Object idObj = attributes.get("memberId");
    if (idObj != null) {
      memberId = Long.valueOf(idObj.toString());
      log.info("[OAuth2_LOG] memberId : {}" , memberId);

      //로그인 성공 시, 로그인 연속 출석 정보 업데이트
      memberService.updateLoginInfo(memberId);

      // ✅출석 점수 부여 로직 호출
      // memberId로 Member 객체를 찾아서 전달합니다.
      Member member = memberRepository.findById(memberId)
              .orElse(null);
      if (member != null) {
        scoreAwardService.awardAttendanceDailyOnce(member);
      }
    }

    Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
    accessTokenCookie.setHttpOnly(true);
    accessTokenCookie.setPath("/");
    accessTokenCookie.setMaxAge(60 * 15);
    response.addCookie(accessTokenCookie);

    Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setMaxAge(60 * 60 * 24 * 3);
    response.addCookie(refreshTokenCookie);



    log.info("login redirect url : {}",  awsEc2IP + "/");
    String redirectUrl = String.format(awsEc2IP + "/");
    response.sendRedirect(redirectUrl);
  }
}